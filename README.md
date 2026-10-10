# TripBoxie

想去的地方，一鍵變成行程。

規劃旅行時，想去的地方常常散落在不同平台，真的要排行程時得到處翻。TripBoxie 讓你先把想去的地方丟進願望清單，地圖會標出已收藏的景點，確定要去再一鍵轉進行程。

<!-- 截圖：把圖片放進 docs/ 資料夾，再把下面這行的檔名換掉 -->
<!-- ![TripBoxie 使用流程](docs/screenshots.png) -->

## 功能

- **旅程管理**：建立旅程、設定日期
- **願望清單**：在地圖上搜尋並收藏想去的地方
- **一鍵加入行程**：從地圖或清單直接把景點排進某一天
- **景點備註與待辦清單**：行前準備收在同一個地方
- **帳號登入**：Firebase Authentication

## 技術

Java · Android SDK · Google Maps SDK / Places API / Geocoding API · Room Database · Firebase Authentication

## 自己執行

這個專案不含任何金鑰，執行前請自行準備：

1. 在 `local.properties` 加入一行：
   ```
   GOOGLE_MAPS_API_KEY=你的金鑰
   ```
   金鑰需啟用 Maps SDK for Android、Places API、Geocoding API。
2. 從 Firebase Console 下載 `google-services.json`，放到 `app/` 資料夾。
3. 用 Android Studio 開啟並執行。

## 作者

陳玟諺（豆特）｜國立宜蘭大學 資訊工程系
