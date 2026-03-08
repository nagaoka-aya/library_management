# Library Management System

書籍と著者の情報を管理するREST APIサービスです。書籍・著者の登録・更新、および著者に紐づく書籍の取得機能を提供します。

## 技術スタック

| 項目 | バージョン |
|---|---|
| 言語 | Kotlin 2.2.21 |
| フレームワーク | Spring Boot 4.0.3 |
| DB | H2 (in-memory) |
| DBマイグレーション | Flyway |
| クエリビルダー | jOOQ |
| テスト | JUnit 5 / MockK 1.14.0 |
| 静的解析 | detekt 1.23.8 |
| Java | 21 |

## 前提条件

- [Docker](https://www.docker.com/) がインストールされていること
- VS Code と [Dev Containers 拡張機能](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) がインストールされていること

## 環境構築

1. このリポジトリをクローンする
2. VS Code でフォルダを開く
3. コマンドパレット（`Ctrl+Shift+P`）から `Dev Containers: Reopen in Container` を実行する

## アプリの起動

```bash
mvn spring-boot:run
```

## 動作確認

| エンドポイント | 説明 | 期待レスポンス |
|---|---|---|
| `GET http://localhost:8080/health` | ヘルスチェック | `{"status":"ok"}` |
| `GET http://localhost:8080/health/db` | DB接続確認 | `{"db":"ok"}` |
| `http://localhost:8080/h2-console` | H2コンソール (JDBC URL: `jdbc:h2:mem:testdb`) | — |

`docs/api/health.http` を REST Client 拡張機能で開くと、エンドポイントを直接実行できます。

## テスト実行

```bash
mvn test
```

## 静的解析

```bash
mvn antrun:run@detekt
```

レポートは `target/reports/detekt.xml` に出力されます。

## ディレクトリ構成

```
.
├── docs/
│   ├── api/          # REST Client 用 .http ファイル
│   ├── spec/         # 要件・DB設計ドキュメント
│   └── task/         # タスク定義
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/example/library_management/
│   │   │       ├── controller/   # REST コントローラー
│   │   │       └── infrastructure/jooq/generated/  # jOOQ 自動生成コード
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/     # Flyway マイグレーションスクリプト
│   └── test/
│       └── kotlin/               # テストコード
├── detekt.yml        # detekt 静的解析設定
└── pom.xml
```
