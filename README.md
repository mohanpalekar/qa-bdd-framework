Perfect ✅ — here’s the final enriched **README.md** with:

* **Sample YAML API spec snippet**
* **Details about cleanup of logs and screenshots in runner/hooks**

---

# 🧩 BDD Test Automation Framework (UI + API)

This is a **Cucumber + Java + Selenium + RestAssured** framework to automate **UI flows** and **API tests** in a simple,
data-driven way.
It supports **dynamic test data generation**, **scenario context storage**, **logging & screenshots**, **YAML-based API
specs**, and **automatic cleanup** of old logs/screenshots.

---

## 🚀 Key Features

* 📝 **Gherkin BDD** – Human-readable feature files.
* 🖱 **UI Actions from DataTables** – All actions (type, click, verify, keyboard, wait) from one step.
* 🌐 **API Calls from YAML** – Define API specs in YAML and override data in steps.
* 🎲 **Dynamic Test Data** – Built-in faker: `${email}`, `${firstname}`, `${myKey=email}` saves to context.
* 📦 **ScenarioContext** – Reuse generated data later in the same scenario: `${context:myKey}`.
* 🛠 **Config-Driven** – Environment & locators from properties files.
* 🖼 **Screenshots & Logs** – Automatic on UI failures, attached to reports.
* 🧹 **Automatic Cleanup** – Old logs & screenshots are cleaned at test start by the runner/hooks.
* ⚡ **Thread-Safe** – Per-scenario context and logger.
* ⏳ **Wait Support** – Use `wait` in UI or API steps to pause execution dynamically.
* ⌨️ **Keyboard Actions** – Send keys like `enter`, `tab`, `ctrl+a`, `ctrl+c`, `ctrl+v` with no element needed.

---

## 📁 Project Structure

```
src
 ├── test
 │    ├── java
 │    │    ├── api/                # API spec loader & payload overwriter
 │    │    ├── context/            # ScenarioContext
 │    │    ├── steps/              # Step Definitions (UI & API)
 │    │    ├── utils/              # DriverFactory, WebDriverUtils, Config, ValueResolver, LogFactory
 │    │    └── runner/             # TestNG/Cucumber runner classes
 │    └── resources
 │         ├── locators.properties   # UI locators
 │         ├── config.properties     # Default environment configs
 │         ├── config.qa.properties  # QA environment configs
 │         ├── config.dev.properties # DEV environment configs
 │         └── api-specs/*.yaml      # API YAML specs
```

---

## 🌍 Environment-Specific Properties

* Default: loads `config.properties`.
* Override at runtime:

```bash
mvn clean test -Denv=qa
```

This picks up `config.qa.properties`. Add any `config.<env>.properties` under `src/test/resources` to support new
environments.

---

## 🎲 Dynamic Test Data – `ValueResolver`

`ValueResolver` dynamically resolves or generates values inside your feature files.
You can use special token syntax in the **`value`** column of the `I perform UI actions` step or in **API override
tables**.

### 🔹 Basic Usage

| Syntax                       | What it does                                     |
|------------------------------|--------------------------------------------------|
| `plainText`                  | Uses the literal value as is                     |
| `${firstname}`               | Generates a random first name                    |
| `${lastname}`                | Generates a random last name                     |
| `${email}`                   | Generates a random email address                 |
| `${phonenumber}`             | Generates a random phone number                  |
| `${address}`                 | Generates a random full address                  |
| `${date:yyyy-MM-dd}`         | Today’s date formatted as `yyyy-MM-dd`           |
| `${futuredate:5:dd/MM/yyyy}` | Date +5 days formatted as `dd/MM/yyyy`           |
| `${number:6}`                | Generates a 6-digit number                       |
| `${string:8}`                | Generates a random alphabetic string of length 8 |

### 🔹 Store & Reuse Values in `ScenarioContext`

| Syntax             | Effect                                                       |
|--------------------|--------------------------------------------------------------|
| `${myKey=email}`   | Generates a random email **and stores it** under key `myKey` |
| `${context:myKey}` | Retrieves the previously saved value under `myKey`           |

### 🔹 Complex Usage Examples

* **Store → Retrieve → Combine**

```gherkin
When I perform UI actions
| operation | locatorKey       | value               |
| type      | form.emailField  | ${userEmail=email}  |
| click     | form.submit      |                     |

Then I perform UI actions
| operation | locatorKey         | value                 |
| verifyText| form.emailConfirm  | ${context:userEmail}  |
```

* **Combine Static + Dynamic**

```gherkin
| operation | locatorKey       | value                                 |
| type      | form.username    | user_${number:4}                      |
| type      | form.password    | Pass@${string:6}                      |
```

* **Date & Future Date**

```gherkin
| operation | locatorKey        | value                   |
| type      | form.startDate    | ${date:yyyy-MM-dd}       |
| type      | form.endDate      | ${futuredate:7:yyyy-MM-dd} |
```

* **Secrets from Vault (placeholder)**

```gherkin
| operation | locatorKey      | value               |
| type      | login.password  | ${vault:mySecretKey}|
```

---

## 🖱 UI Actions Supported

| Operation     | UI Step Action                                         |
|---------------|--------------------------------------------------------|
| `type`        | Types text into locator                                |
| `click`       | Clicks on locator                                      |
| `verifyText`  | Verifies text of locator                               |
| `mouseOver`   | Moves mouse over locator                               |
| `doubleClick` | Double-click locator                                   |
| `keyboard`    | Sends key actions (`enter`, `tab`, `ctrl+a`, `ctrl+c`) |
| `wait`        | Pauses execution for specified seconds                 |

---

## 🌐 API Steps Supported

* `When I call api spec "{yaml}" without overrides`
* `When I call api spec "{yaml}" with overrides`

You can:

| Override Key    | Meaning                           |
|-----------------|-----------------------------------|
| `path:myParam`  | Adds a path parameter             |
| `query:myParam` | Adds a query parameter            |
| `wait`          | Waits before sending the request  |
| other keys      | Added to JSON payload as override |

All values (including dynamic/faker) are resolved through `ValueResolver`.

---

## 📝 Sample YAML API Spec

Create a file under `src/test/resources/api-specs/create_user.yaml`:
Perfect — you’re already structuring your YAML specs for **body reuse** 🎯

In your snippet:

```yaml
name: createUser
method: POST
uri: "/v1/users/{tenantId}"
headers:
  Content-Type: application/json
  Accept: application/json
pathParams:
  tenantId: "~"
query:
  verbose: "true"
payload: "create_user.json"
```

`payload: "create_user.json"` means your framework should pick up a JSON file from your resources and use it as the
request body.

Here’s how we normally set it up (and how to document it):

---

### 📝 How Body Payload Works

1. In your YAML spec, you reference a `.json` file under `payload:` instead of writing inline JSON.
2. The framework’s `ApiYamlSpec` loader should already resolve it as:

* Look for `src/test/resources/api-specs/payloads/create_user.json` (or wherever you keep JSON payloads).
* Load the JSON file contents into a string.
* If any overrides are passed from the step table (the “key/value” rows in your feature), they’re applied via
  `PayloadOverwriter` to update fields inside the JSON dynamically.

---

### 📂 Project Structure for API Payloads

```
src/test/resources/api-specs
 ├── create_user.yaml       # The API spec
 ├── get_objects.yaml
 └── payloads/
      └── create_user.json  # Request body referenced by YAML
```

---

### 🔹 Example JSON Payload (`create_user.json`)

```json
{
  "name": "${firstname}",
  "email": "${userEmail=email}",
  "password": "Pass@${string:8}"
}
```

The `ValueResolver` will process the dynamic tokens in this JSON automatically before sending the request.

---

### 🔹 Example YAML Spec (`create_user.yaml`)

```yaml
name: createUser
method: POST
uri: "/v1/users/{tenantId}"
headers:
  Content-Type: application/json
  Accept: application/json
pathParams:
  tenantId: "~"              # placeholder, overridden in step
query:
  verbose: "true"
payload: "payloads/create_user.json"
```

---

### 🔹 Feature File Usage

```gherkin
Scenario: Create user with dynamic data
When I call api spec "create_user.yaml" with overrides
| key         | value             |
| path:tenantId | 1234            |
| wait        | 3                 |
Then response status should be 201
And response json should match
| key   | value                |
| email | ${context:userEmail} |
```

What happens under the hood:

* The loader reads `create_user.yaml`.
* It finds `payload: payloads/create_user.json` and loads that file.
* `PayloadOverwriter` applies any overrides you passed in your step.
* `ValueResolver` replaces tokens inside the JSON like `${firstname}` or `${context:userEmail}` before sending.
* RestAssured sends the POST request with the final JSON.

---

### 🧹 Logs & Screenshots Cleanup

This doesn’t change: at the beginning of your runner or hooks, you still clear old `target/screenshots` and
`target/logs` directories.
This keeps each run clean and prevents stale data from previous runs mixing with new runs.

---

Would you like me to add this **payload workflow section** into your README (the one we’ve been building)? It would show
your testers exactly where to put `.json` payloads and how the overrides + `ValueResolver` work together.

---

## 📦 Scenario Context

* Thread-safe per scenario using `ThreadLocal`.
* Stores generated values and API responses.
* Cleared automatically after each scenario in `Hooks`.

---

## 🖼 Logging, Screenshots & Cleanup

* **Logs:** Per-scenario logger via `Hooks.getLogger()`. Logs all UI actions, waits, API calls, and response details.
* **Screenshots:** Captures automatically when a UI scenario fails. Saved under `target/screenshots/`.
* **Cleanup:** Before any test run starts, your `Runner` or `@BeforeAll` hook deletes previous logs and screenshots (
  under `target/logs` and `target/screenshots`) to ensure a clean run. This prevents old evidence from mixing with new
  results.

Example pseudo-code in runner:

```java

@BeforeAll
public static void cleanUp() {
    FileUtils.cleanDirectory(new File("target/screenshots"));
    FileUtils.cleanDirectory(new File("target/logs"));
}
```

---

## 🔄 Flow Diagram

```text
 Feature File (Gherkin)
        |
        v
 Step Definitions (UISteps / ApiSteps)
        |
        v
 ValueResolver --> generates or fetches dynamic data
        |
        v
 ScenarioContext (thread-local)
        |
        +--> WebDriverUtils / Selenium (UI)
        |
        +--> RestAssured (API)
        |
        v
 Logs + Screenshots + Reports
```

---

## ✅ Benefits Recap

* One flexible UI step handles all operations.
* Unified dynamic data generator for UI & API.
* ScenarioContext to share values between steps.
* Per-scenario logging and automatic screenshots.
* **Automatic cleanup** of logs/screenshots for each run.
* Easy parallel execution with thread-safe storage.
* YAML-based API specs for cleaner maintenance.
* Environment-specific properties built-in.

---

## 🏁 Quick Start

```bash
git clone https://your-repo.git
cd your-repo
mvn clean install
mvn clean test
```

## ⚡ Parallel Execution

This framework supports parallel runs via the **cucumber-jvm-parallel-plugin**.

- **SCENARIO-level parallelism** (default): each scenario runs in its own thread.
- **FEATURE-level parallelism**: each feature file runs in its own thread.

We’ve configured both in `pom.xml` using multiple executions and a Maven profile.

### Run SCENARIO-level (default)

### Run All Scenarios (TestNG + Cucumber)

mvn clean test


### Run with Feature-level parallelism

mvn clean test -Pfeature-parallel


### Override Threads at Runtime

mvn clean test -DthreadCount=8 -Dparallel=methods

```bash
mvn clean test


mvn clean test -Pfeature-parallel


Run by tag:

```bash
mvn clean test -Dcucumber.filter.tags="@ui or @api"
```

Reports under `target/`, screenshots under `target/screenshots/`.

---