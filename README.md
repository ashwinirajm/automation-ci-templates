# automation-ci-templates

![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-CI%2FCD-blue?logo=githubactions)
![CI Templates](https://img.shields.io/badge/CI-Templates-success)
![Test Automation](https://img.shields.io/badge/Test-Automation-orange)
![YAML](https://img.shields.io/badge/YAML-Workflows-blueviolet)
![License](https://img.shields.io/github/license/ashwinirajm/automation-ci-templates)

Reusable GitHub Actions CI templates for test automation.

# PR Compatibility Check
The goal of this workflow is to fail fast and catch common issues early, such as:
- Broken builds
- Dependency conflicts
- Java version incompatibility
- Invalid project structure
It is intentionally lightweight and does not run tests, making it fast and reliable for pull request validation.

# Test Execution with Failure Analysis
The workflow runs tests by environment and suite, captures logs, and performs failure signature analysis to categorize test failures directly in CI.
- Environment-based execution (qa, prod)
- Suite-based execution (smoke, regression)
- Automatic failure categorization: **TIMEOUT, SERVER_500, ASSERTION_FAILURE, UNKNOWN_FAILURE**
- Occurrence count per failure type

<img width="800" height="500" alt="Screenshot 2026-01-08 at 12 44 53 AM" src="https://github.com/user-attachments/assets/502f97ab-68e4-4365-87e9-d0127d299d71" />

# Mobile Automation – Android & iOS
The workflow is used for running Android and iOS tests using Maven. It is designed to be self-hosted runner friendly, customizable, and reusable.
- Supports Android & iOS test execution
- Runs on self-hosted macOS runners
- Uploads test reports as artifacts
- Triggers: **Manual (workflow_dispatch), Scheduled (weekly via cron)**
- Configurable environment, thread count, and suite XML
