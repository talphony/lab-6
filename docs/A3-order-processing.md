# Блок A3 «Обработать заказ»: соответствие IDEF0 и кода

Модуль: `com.example.fbs.orderprocessing` (пакеты слоёв внутри модуля)

## Слои

| Пакет | Что лежит |
|---|---|
| `controller` | `OrderProcessingController` — REST: ручной запуск A3 и последний отчёт |
| `scheduler` | `OrderProcessingScheduler` — запуск A3 по расписанию (компонент «Планировщик» на C4 L3) |
| `service` | Бизнес-логика: A3 и атомарные функции A31–A35 |
| `repository` | Интерфейсы хранилищ и их заглушки в памяти |
| `integration` | Коннекторы площадок и передача заданий в A4: интерфейсы и заглушки |
| `config` | Управляющие параметры: правила площадок, страховой буфер, включение расписания |
| `model` | Доменные объекты: заказ, строка заказа, резерв, статусы |
| `dto` | Результаты операций и отчёт, который отдаёт REST |
| `demo` | Демо-режим для ручной проверки: наполнение заглушек и просмотр их состояния |

## Функции IDEF0

| IDEF0 | Класс (`service`) | Метод |
|---|---|---|
| A3 Обработать заказ | `OrderProcessingService` | `process(since)`, `runSinceLastRun()` |
| A31 Загрузить заказы | `OrderLoader` | `load(marketplace, since)` |
| A32 Обработать отмену | `CancellationHandler` | `handle(order)` |
| A33 Зарезервировать товар | `ReservationService` | `reserve(order)` |
| A34 Пересчитать доступный остаток | `AvailableStockCalculator` | `calculate(skus)` |
| A35 Синхронизировать остаток | `StockSynchronizer` | `sync(updates)` |

## Стрелки

| Стрелка IDEF0 | Тип | В коде |
|---|---|---|
| Заказ с маркетплейсов | вход A3, A31 | `MarketplaceOrder` из `MarketplaceConnector.fetchNewOrders()` |
| Актуальные остатки | вход A3, A33, A34 | `StockRepository.onHand()` |
| Новый заказ | из A31 в A33 | `LoadResult.newOrders()` |
| Отмена | из A31 в A32 | `LoadResult.cancelledOrders()` |
| Снятый резерв | из A32 в A34 | `List<Reservation>` из `CancellationHandler.handle()` |
| Резерв | из A33 в A34 | `Reservation` в `ReservationRepository` |
| Доступный остаток | из A34 в A35 | `StockUpdate` |
| Остатки для маркетплейса | выход A3 | `MarketplaceConnector.pushStocks()`, итог в `SyncResult` |
| Задание на сборку | выход A3, в A4 | `AssemblyTask` через `AssemblyTaskPublisher` |
| Правила маркетплейсов | управление | `MarketplaceRules` (размер пачки) |
| Страховой буфер | управление | `SafetyBufferPolicy`, настройка `wms.safety-buffer` |
| WMS | механизм | Spring-бины модуля |

## Заглушки

Всё за границей A3 заменено простыми классами в памяти:

- `integration.StubMarketplaceConnector` — вместо HTTP-коннекторов WB, Ozon, ЯМ;
- `repository.InMemoryStockRepository` — вместо остатков, которые ведёт A2;
- `repository.InMemoryOrderRepository`, `repository.InMemoryReservationRepository` — вместо PostgreSQL;
- `config.DefaultMarketplaceRules`, `config.FixedSafetyBufferPolicy` — вместо настроек компании;
- `integration.CollectingAssemblyTaskPublisher` — вместо передачи заданий в A4.

Чтобы подключить БД, достаточно реализовать интерфейсы `repository` через Spring Data JPA: сервисы менять не нужно.

## REST

| Метод | Путь | Что делает |
|---|---|---|
| POST | `/api/order-processing/run` | Запустить A3 сейчас, вернуть отчёт |
| GET | `/api/order-processing/last-report` | Отчёт последнего прогона (204, если прогонов не было) |

## Правила

- Доступно = на складе − резерв − страховой буфер, не меньше 0.
- Резерв «всё или ничего»: не хватило одной позиции, заказ уходит в `NOT_ENOUGH_STOCK`.
- Резерв проверяется по свободному остатку без буфера: заказ уже продан.
- Внутри прогона сначала обрабатываются отмены, потом новые заказы.
- Ошибка одной площадки при выгрузке не мешает остальным.

## Настройки (`application.properties`)

```
wms.safety-buffer=0
wms.order-processing.interval=PT2M
wms.order-processing.scheduler-enabled=true
wms.demo.enabled=true
```

## Проверка

### Автотесты

```
mvnw.cmd test
```

- `service/*Test` — каждая атомарная функция A31–A35 и A3 отдельно, без Spring;
- `OrderProcessingModuleTest` — модуль целиком в Spring-контексте на демо-данных.

### Вручную

1. Запустить `FbsApplication`.
2. Открыть `http/a3-demo.http` в IntelliJ и выполнять запросы по порядку.

Демо-эндпоинты (включены, пока `wms.demo.enabled=true`):

| Метод | Путь | Что делает |
|---|---|---|
| POST | `/api/demo/seed` | Заполнить: 4 товара, 4 заказа с WB, Ozon, ЯМ |
| GET | `/api/demo/state` | Остатки, заказы со статусами, резервы, задания на сборку, что ушло на площадки |
| PUT | `/api/demo/stock` | Задать остаток товара |
| POST | `/api/demo/orders` | Новый заказ на площадке |
| POST | `/api/demo/orders/{marketplace}/{externalId}/cancel` | Отмена заказа на площадке |
| POST | `/api/demo/reset` | Очистить всё |

Ожидаемый результат первого прогона после `seed` (буфер 0): загружено 4, зарезервировано 3,
нехватка 1 (Ozon OZ-2001, кружки: нужно 5, есть 2), на каждую площадку ушла одна пачка остатков.
