# AttendanceApp ビルド環境 ネットワーク要件・プロキシ許可ドメイン一覧

## 概要

本プロジェクト（AttendanceApp）を Android Studio でビルドする際、Gradle ビルドシステムが
各種ライブラリやプラグインを外部リポジトリからダウンロードします。

プロキシ環境やファイアウォール環境で開発する場合、以下のドメインへの HTTPS(443) 通信を
許可する必要があります。また、オフライン環境に持ち込む場合の手順も記載します。

---

## 1. 許可が必要なドメイン一覧

### 1.1 必須ドメイン（ビルドに必須）

| # | ドメイン | ポート | 用途 | 設定箇所 |
|---|---------|--------|------|----------|
| 1 | `services.gradle.org` | 443 | Gradle 本体のダウンロード (gradle-9.3.1-bin.zip) | `gradle/wrapper/gradle-wrapper.properties` |
| 2 | `maven.google.com` | 443 | Google / AndroidX / AGP ライブラリの取得 | `settings.gradle.kts` → `google()` |
| 3 | `dl.google.com` | 443 | Google Maven の一部リダイレクト先 | `google()` リポジトリ経由 |
| 4 | `repo.maven.apache.org` | 443 | Maven Central（Kotlin, JUnit, Coroutines 等） | `settings.gradle.kts` → `mavenCentral()` |
| 5 | `plugins.gradle.org` | 443 | Gradle プラグインポータル | `settings.gradle.kts` → `gradlePluginPortal()` |
| 6 | `api.foojay.io` | 443 | JVM ツールチェーン自動プロビジョニング | `gradle/gradle-daemon-jvm.properties` |

### 1.2 リダイレクト・CDN 関連（上記ドメインのリダイレクト先）

| # | ドメイン | 用途 |
|---|---------|------|
| 7 | `*.googleapis.com` | Google Maven の CDN |
| 8 | `repo1.maven.org` | Maven Central のミラー/リダイレクト先 |
| 9 | `jcenter.bintray.com` | 一部レガシー依存の参照先（通常不要だが念のため） |
| 10 | `github.com` / `objects.githubusercontent.com` | Foojay 経由の JDK ダウンロードリダイレクト先 |

### 1.3 Android Studio / SDK 関連（IDE 自体の更新・SDK 管理）

| # | ドメイン | 用途 |
|---|---------|------|
| 11 | `dl.google.com` | Android SDK コンポーネントのダウンロード |
| 12 | `developer.android.com` | SDK ライセンス確認等 |

---

## 2. 本プロジェクトの依存ライブラリと取得先の対応

### 2.1 Google Maven (`maven.google.com`) から取得されるもの

| ライブラリ | バージョン |
|-----------|-----------|
| `com.android.application` (AGP) | 9.1.0 |
| `androidx.core:core-ktx` | 1.18.0 |
| `androidx.appcompat:appcompat` | 1.6.1 |
| `androidx.activity:activity` | 1.13.0 |
| `androidx.constraintlayout:constraintlayout` | 2.1.4 |
| `androidx.room:room-runtime` / `room-compiler` / `room-ktx` | 2.7.1 |
| `androidx.lifecycle:lifecycle-viewmodel-ktx` / `lifecycle-livedata-ktx` | 2.8.7 |
| `androidx.navigation:navigation-fragment-ktx` / `navigation-ui-ktx` | 2.8.6 |
| `androidx.test.ext:junit` | 1.3.0 |
| `androidx.test.espresso:espresso-core` | 3.7.0 |
| `com.google.android.material:material` | 1.10.0 |
| `com.google.android.gms:play-services-location` | 21.3.0 |
| `com.google.devtools.ksp` | 2.1.20-1.0.32 |

### 2.2 Maven Central (`repo.maven.apache.org`) から取得されるもの

| ライブラリ | バージョン |
|-----------|-----------|
| `org.jetbrains.kotlin.android` (Kotlin プラグイン) | 2.1.20 |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | 1.9.0 |
| `junit:junit` | 4.13.2 |

### 2.3 Gradle Plugin Portal (`plugins.gradle.org`) から取得されるもの

| プラグイン | バージョン |
|-----------|-----------|
| `org.gradle.toolchains.foojay-resolver-convention` | 1.0.0 |

### 2.4 Gradle 本体 (`services.gradle.org`)

| ファイル | バージョン |
|---------|-----------|
| `gradle-9.3.1-bin.zip` | 9.3.1 |

---

## 3. プロキシ設定方法

### 3.1 Gradle のプロキシ設定

`gradle.properties`（プロジェクトルート or `~/.gradle/gradle.properties`）に以下を追加:

```properties
# HTTP プロキシ
systemProp.http.proxyHost=proxy.example.com
systemProp.http.proxyPort=8080
systemProp.http.proxyUser=username
systemProp.http.proxyPassword=password
systemProp.http.nonProxyHosts=localhost|127.0.0.1

# HTTPS プロキシ
systemProp.https.proxyHost=proxy.example.com
systemProp.https.proxyPort=8080
systemProp.https.proxyUser=username
systemProp.https.proxyPassword=password
systemProp.https.nonProxyHosts=localhost|127.0.0.1
```

### 3.2 Android Studio のプロキシ設定

1. **File** → **Settings** → **Appearance & Behavior** → **System Settings** → **HTTP Proxy**
2. **Manual proxy configuration** を選択
3. Host / Port / 認証情報を入力
4. **Check connection** で `https://maven.google.com` への接続を確認

### 3.3 SSL 証明書の問題（自己署名証明書を使うプロキシの場合）

プロキシが SSL インスペクションを行っている場合、CA 証明書の追加が必要:

```bash
# プロキシの CA 証明書を Java のトラストストアに追加
keytool -importcert -file proxy-ca.crt -keystore "$JAVA_HOME/lib/security/cacerts" -alias proxy-ca

# もしくは Gradle に個別指定
# gradle.properties に追加:
systemProp.javax.net.ssl.trustStore=/path/to/custom-truststore.jks
systemProp.javax.net.ssl.trustStorePassword=changeit
```

---

## 4. オフライン環境への持ち込み手順

### 4.1 方法A: Gradle キャッシュの丸ごとコピー（推奨・簡単）

オンライン環境で一度ビルドを成功させた後、Gradle のキャッシュをコピーします。

**手順:**

1. オンライン環境でビルドを実行:
   ```bash
   ./gradlew assembleDebug
   ```

2. 以下のディレクトリをオフライン環境にコピー:
   ```
   コピー元（Windows）:
     %USERPROFILE%\.gradle\caches\
     %USERPROFILE%\.gradle\wrapper\dists\

   コピー先（オフライン環境の同じパス）:
     %USERPROFILE%\.gradle\caches\
     %USERPROFILE%\.gradle\wrapper\dists\
   ```

3. オフライン環境の `gradle.properties` に追加:
   ```properties
   org.gradle.offline=true
   ```

4. Android Studio のメニューから:
   **File** → **Settings** → **Build, Execution, Deployment** → **Gradle** →
   **Offline work** にチェック

### 4.2 方法B: ローカル Maven リポジトリの構築（組織向け）

社内に Maven リポジトリマネージャ（Nexus, Artifactory 等）を立て、
必要なライブラリをミラーリングする方法です。

1. Nexus/Artifactory をセットアップし、以下のリモートリポジトリをプロキシ:
   - `https://maven.google.com`
   - `https://repo.maven.apache.org/maven2`
   - `https://plugins.gradle.org/m2`

2. `settings.gradle.kts` のリポジトリ URL を社内サーバに変更:
   ```kotlin
   pluginManagement {
       repositories {
           maven { url = uri("https://nexus.internal.example.com/repository/google/") }
           maven { url = uri("https://nexus.internal.example.com/repository/maven-central/") }
           maven { url = uri("https://nexus.internal.example.com/repository/gradle-plugins/") }
       }
   }
   dependencyResolutionManagement {
       repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
       repositories {
           maven { url = uri("https://nexus.internal.example.com/repository/google/") }
           maven { url = uri("https://nexus.internal.example.com/repository/maven-central/") }
       }
   }
   ```

---

## 5. よくあるエラーと対処法

| エラーメッセージ | 原因 | 対処 |
|----------------|------|------|
| `Could not resolve com.android.tools.build:gradle:X.X.X` | `maven.google.com` への接続失敗 | プロキシ許可リストに `maven.google.com` を追加 |
| `Could not GET 'https://repo.maven.apache.org/...'` | Maven Central への接続失敗 | プロキシ許可リストに `repo.maven.apache.org` を追加 |
| `Connection timed out` | プロキシ設定の不備 | `gradle.properties` のプロキシ設定を確認 |
| `PKIX path building failed` / `SSL handshake` | SSL インスペクションによる証明書エラー | プロキシの CA 証明書をトラストストアに追加（3.3 節参照） |
| `Could not resolve org.jetbrains.kotlin:...` | Kotlin 関連ライブラリの取得失敗 | `repo.maven.apache.org` と `maven.google.com` の両方を許可 |
| `No cached version of gradle-X.X.X-bin.zip` | Gradle 本体が未キャッシュ | `services.gradle.org` を許可、またはオフライン用に手動配置 |
| `Could not resolve all artifacts for configuration ':classpath'` | 複数リポジトリの接続失敗 | 全必須ドメイン（セクション1.1）を確認 |

---

## 6. 接続確認コマンド

プロキシ設定後、以下のコマンドで各ドメインへの接続を確認できます:

```bash
# 各ドメインへの疎通確認
curl -I https://services.gradle.org
curl -I https://maven.google.com
curl -I https://repo.maven.apache.org/maven2/
curl -I https://plugins.gradle.org/m2/
curl -I https://api.foojay.io

# Gradle 経由での依存解決テスト（プロジェクトルートで実行）
./gradlew dependencies --info
```

---

## 7. ファイアウォール申請用サマリ

ネットワーク管理者への申請時に使用できる簡潔な一覧:

```
許可対象プロトコル: HTTPS (TCP/443)

必須ドメイン:
  - services.gradle.org       ... Gradle ビルドツール本体
  - maven.google.com          ... Android / Google ライブラリ
  - dl.google.com             ... Google Maven CDN / Android SDK
  - repo.maven.apache.org     ... Java / Kotlin 汎用ライブラリ
  - repo1.maven.org           ... Maven Central ミラー
  - plugins.gradle.org        ... Gradle プラグイン
  - api.foojay.io             ... JVM ツールチェーン

推奨追加（CDN・リダイレクト先）:
  - *.googleapis.com          ... Google CDN
  - objects.githubusercontent.com ... JDK ダウンロードリダイレクト先
```

---

*本ドキュメントは AttendanceApp プロジェクトの `libs.versions.toml` および `settings.gradle.kts` の内容に基づいて作成されています。依存ライブラリの追加・変更時は適宜更新してください。*
