# 🛠 automation-ci-templates

![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-CI%2FCD-blue?logo=githubactions)
![CI Templates](https://img.shields.io/badge/CI-Templates-success)
![Test Automation](https://img.shields.io/badge/Test-Automation-orange)
![YAML](https://img.shields.io/badge/YAML-Workflows-blueviolet)
![License](https://img.shields.io/github/license/ashwinirajm/automation-ci-templates)

This repo provides reusable GitHub Actions CI templates designed to simplify and standardize continuous integration workflows, especially for test automation across projects

# Templates Included :
- [pr-compatibility-check.yml](.github/workflows/pr-compatibility-check.yml) - Checks for compatibility, errors, or code issues whenever a pull request is raised.
  
- [test-execution-with-failure-analysis.yml](.github/workflows/test-execution-with-failure-analysis.yml) - Supports environment- and suite-based execution, automatically categorizes failures (TIMEOUT, SERVER_500, ASSERTION_FAILURE, UNKNOWN_FAILURE), and tracks occurrence counts for each failure type.

- [scheduled-mobile-tests.yml](.github/workflows/scheduled-mobile-tests.yml) - Supports Android and iOS test execution on self-hosted macOS runners, uploads test reports as artifacts, triggers manually or on a weekly schedule, and allows configurable environment, thread count, and suite XML.

- [rerun-common-failures.yml](.github/workflows/rerun-common-failures.yml) - Analyzes test failures and selectively reruns those caused by common transient issues like timeouts, HTTP 500.
