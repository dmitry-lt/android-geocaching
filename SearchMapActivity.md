# Определение положения #

## Ограничения на провайдеров положения ##
Данная активити работает лишь при включенном самом точном провайдере положения на устройстве. Если же этот провайдер выключен, то активити запускается как обычно (подписывается на все нужные ей листнеры), но её экран блокируется диалогом, описанным ниже.

### Диалог "Включить GPS?" ###
Данный диалог отображается, когда самый точный провайдер [выключен](LocationManager#isBestProviderEnabled.md), как только он будет включен - диалог исчезнет. Данный диалог нельзя закрыть (`cancelable = false`).

У этого диалога есть следующие кнопки:
  * "Да" - открывает экран настроек `Settings.ACTION_LOCATION_SOURCE_SETTINGS`
  * "Нет" - финиширует данную активити

Как только активити возобновляется (метод `onResume`) проверяется включен ли самый точный провайдер положения. Если нет, то отображается диалог. Если в процессе работы пользователь включит/выключит GPS (свернув приложение или используя статусную панель), то диалог исчезнет или отобразится.

## Индикация ##
### Индикация определения положения ###
При старте активити, если нет [точного](http://code.google.com/p/android-geocaching/wiki/SearchMapActivity#Точное_положение) положения, отображается прогресс бар. Как только определится положение - он исчезает. Как только статус станет TEMPORARILY\_UNAVAILABLE или OUT\_OF\_SERVICE он вновь отобразится и исчезнет при очередном обновлении положения.

### Индикация состояния gps ###
Статусная строчка отображается постоянно и сообщает состояние gps
  * Инициализация (пока не придёт сообщение о количестве спутников)
  * Количество спутников
  * Недоступен (когда статус OUT\_OF\_SERVICE)
Она обновляется в методе `onStatusChanged`.
### Индикация расстояния до тайника ###
Если определено положение, то расстояние до тайника отображается целым числом в метрах, если оно меньше 1000 метров, иначе с одним знаком после запятой в километрах.
### Индикация точности положения ###
Если положение определено не точно, то перед расстоянием также выводится знак приближённо равно (≈), а стрелка компаса становится серой.

## Точное положение ##
Положение считается точным если оно было получено менее чем 30 секунд назад и определено точнее 40 метров. Если положение не определено, то оно считается неточным.

## Частота обновления ##
Частота обновления устанавливается в настройках.
При приближении к тайнику ближе чем на 100 метров включается режим максимально частого обновления.

## Направление (компас) ##
Используемый датчик указывается в настройках.
При скорости выше 20 км/ч используется информация от gps
не зависимо от настроек.