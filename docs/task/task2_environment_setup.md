# Task 2: 環境構築

## 概要
Flyway によるDBマイグレーション環境を整備し、jOOQ のコード生成を設定・実行することで、実装に必要な基盤を整える。

---

## 作業内容

### 1. Flyway 依存追加と application.properties 設定

#### 入力

- `pom.xml`（既存）
- `src/main/resources/application.properties`（既存）

#### 出力

- `pom.xml` に以下の依存を追加する
  - `org.flywaydb:flyway-core`
- `src/main/resources/application.properties` に以下を追加する
  - H2 インメモリDB の接続設定（`spring.datasource.url`, `username`, `password`）
  - H2 コンソールの有効化（`spring.h2.console.enabled=true`）
  - Flyway の有効化（`spring.flyway.enabled=true`）

#### 備考

- DBは H2 インメモリを使用する（既存の依存に含まれている）
- H2 の `MODE=MySQL` など互換モードは使用せずシンプルな設定にとどめる

---

### 2. DDL マイグレーションスクリプト作成

#### 入力

- `docs/spec/db_structure.md`（Task 1-1 の成果物）

#### 出力

- `src/main/resources/db/migration/V1__create_tables.sql`
  - `authors` テーブルの CREATE TABLE 文
  - `books` テーブルの CREATE TABLE 文
  - `book_authors` テーブルの CREATE TABLE 文

#### 備考

- ファイル名は Flyway の命名規則 `V{バージョン}__{説明}.sql` に従う
- H2 で動作する SQL 構文で記述する

---

### 3. jOOQ コード生成設定と実行

#### 入力

- `pom.xml`（既存）
- `src/main/resources/db/migration/V1__create_tables.sql`（本タスク 2-2 の成果物）

#### 出力

- `pom.xml` の `<build><plugins>` に jOOQ Generator プラグイン設定を追加する
  - H2 を使って Flyway マイグレーション後のスキーマからコードを生成する設定
  - 生成先パッケージ: `com.example.library_management.infrastructure.jooq.generated`
  - 生成先ディレクトリ: `src/main/kotlin`（生成後 `target/generated-sources` に変更も可）
- `mvn generate-sources` 実行により以下が自動生成されることを確認する
  - 各テーブルに対応する `Tables`、`Records` クラス

#### 備考

- `jooq-codegen-maven` プラグインは `org.jooq` グループを使用する
- Flyway と組み合わせることで、DDL と生成コードの一致を保つ
- 生成コードはコミット対象に含めるかどうかをチームで決定する（今回はコミット対象とする）

---

### 4. 静的解析ツール設定

#### 入力

- `pom.xml`（既存）

#### 出力

- `pom.xml` に以下のプラグインを追加する
  - `io.gitlab.arturbosch.detekt:detekt-maven-plugin` — Kotlin 向け静的解析
- `detekt.yml` をプロジェクトルートに作成し、ルールセットを設定する
  - 基本的にはデフォルトルールを使用し、過剰な指摘を避けるため必要に応じてルールを無効化する

#### 備考

- `mvn detekt:check` で静的解析を実行できることを確認する
- ktlint はコードフォーマット専用のため今回は導入しない（detekt のみで十分）

---

### 5. 単体テストライブラリ設定

#### 入力

- `pom.xml`（既存）

#### 出力

- `pom.xml` の `<dependencies>` に以下を追加する
  - `io.mockk:mockk` — Kotlin 向けモックライブラリ
  - `org.springframework.boot:spring-boot-starter-test` — Spring Boot テストサポート（MockMvc 含む）
- テスト用 `application.properties` を追加する
  - `src/test/resources/application.properties`
  - H2 インメモリDB を使用するテスト用接続設定

#### 備考

- JUnit 5 は既存の `kotlin-test-junit5` で対応済み
- MockK を使うことで Kotlin の `object`・`companion object`・拡張関数のモックも可能
- `@SpringBootTest` / `@WebMvcTest` / 純粋なユニットテストの使い分け方針を決めておく（今回は Service 層は MockK で純粋なユニットテスト、Controller 層は `@WebMvcTest` を使用する）

---

### 6. devcontainer VS Code 拡張機能設定

#### 入力

- `.devcontainer/devcontainer.json`（既存）

#### 出力

- `.devcontainer/devcontainer.json` の `customizations.vscode.extensions` に以下を追加する

| 拡張機能ID | 用途 |
|---|---|
| `mathiasfrohlich.Kotlin` | Kotlin 言語サポート（シンタックスハイライト） |
| `fwcd.kotlin` | Kotlin Language Server（補完・定義ジャンプ） |
| `vscjava.vscode-java-pack` | Java/Kotlin 開発基盤（デバッガー・テストランナー） |
| `vscjava.vscode-maven` | Maven ライフサイクル・ゴールのGUI実行 |
| `detekt.detekt` | detekt 静的解析のエディタ内リアルタイム表示 |
| `esbenp.prettier-vscode` | YAMLなど設定ファイルのフォーマッター |
| `redhat.vscode-yaml` | `application.properties` / `openapi.yaml` の補完・検証 |
| `humao.rest-client` | `.http` ファイルでAPIを直接呼び出してテスト |

#### 備考

- ktlint のフォーマッターは detekt プラグインに含まれるため個別インストール不要
- `humao.rest-client` を使い、`docs/api/` 配下に `.http` ファイルを置くことで API の動作確認を手軽に行える

---

### 7. Hello World API 実装と起動確認

#### 入力

- `src/main/kotlin/com/example/library_management/LibraryManagementApplication.kt`（既存）

#### 出力

- `src/main/kotlin/com/example/library_management/controller/HealthController.kt`
  - `GET /health` — `{"status": "ok"}` を返す簡易ヘルスチェックエンドポイント
  - `GET /health/db` — `DSLContext` を使って `SELECT 1` を実行し DB 接続を確認するエンドポイント（`{"db": "ok"}` を返す）
- `docs/api/health.http`
  - `GET /health` のリクエスト例
  - `GET /health/db` のリクエスト例

#### 動作確認手順

1. `mvn spring-boot:run` でアプリを起動する
2. Flyway マイグレーションがエラーなく完了することをログで確認する
3. `docs/api/health.http` の `GET /health` を実行し 200 OK と `{"status": "ok"}` が返ることを確認する
4. `docs/api/health.http` の `GET /health/db` を実行し 200 OK と `{"db": "ok"}` が返ることでアプリからDBへの接続を確認する
5. `http://localhost:8080/h2-console` にアクセスし H2 コンソールに接続できることを確認する
5. H2 コンソールで以下の SQL を実行し、テーブルが正常に作成されていることを確認する
   ```sql
   SHOW TABLES;
   SELECT * FROM authors;
   SELECT * FROM books;
   SELECT * FROM book_authors;
   ```

#### 備考

- このタスクが完了すれば、Flyway・jOOQ・Spring Boot の基盤が正常に動作していることを確認できる
- `HealthController` は Task 3 以降のビジネスロジック実装では使用しないが、削除はせず残す

---

### 8. README 作成

#### 入力

- `pom.xml`（既存）
- `.devcontainer/devcontainer.json`（Task 2-6 の成果物）
- `docs/spec/requirements.md`

#### 出力

- `README.md`（プロジェクトルートに新規作成）
  - **プロジェクト概要** — システムの目的と機能概要
  - **技術スタック** — 言語・フレームワーク・ライブラリのバージョン一覧
  - **前提条件** — Dev Container、Docker の使用を前提とする旨
  - **環境構築手順** — Dev Container の起動方法
  - **アプリの起動方法** — `mvn spring-boot:run` コマンド
  - **動作確認** — `GET /health`、`GET /health/db`、H2 コンソールのURL
  - **テスト実行方法** — `mvn test` コマンド
  - **静的解析の実行方法** — `mvn detekt:check` コマンド
  - **ディレクトリ構成** — 主要なディレクトリと役割の説明

#### 備考

- 過剰に詳細にせず、開発者がすぐに動かせる最低限の情報にとどめる
