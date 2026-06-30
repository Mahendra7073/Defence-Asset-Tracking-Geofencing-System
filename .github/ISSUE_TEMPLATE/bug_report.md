name: Bug report
about: Create a report to help us improve and fix issues
title: '[BUG] '
labels: bug, triage
assignees: ''
body:
  - type: markdown
    attributes:
      value: |
        Thank you for reporting a bug! Please fill out the details below to help us reproduce and address it.
  - type: textarea
    id: description
    attributes:
      label: Bug Description
      description: A clear and concise description of what the bug is.
      placeholder: Describe the issue...
    validations:
      required: true
  - type: textarea
    id: reproduction
    attributes:
      label: Steps to Reproduce
      description: List the steps to reproduce the behavior.
      placeholder: |
        1. Go to '...'
        2. Click on '...'
        3. Scroll down to '...'
        4. See error
    validations:
      required: true
  - type: textarea
    id: expected
    attributes:
      label: Expected Behavior
      description: A clear and concise description of what you expected to happen.
      placeholder: What should have occurred...
    validations:
      required: true
  - type: textarea
    id: context
    attributes:
      label: Context & Environment
      description: Provide system configuration details (OS, Browser, JDK version, database version, etc.).
      value: |
        - OS: Windows / Linux / macOS
        - Browser: Chrome / Firefox / Safari / Edge
        - JDK Version: 17
        - Tomcat Version: 9.0
        - PostgreSQL / PostGIS Version: 15+ / 3+
    validations:
      required: true
