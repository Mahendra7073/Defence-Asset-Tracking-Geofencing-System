name: Feature request
about: Suggest an idea or enhancement for this project
title: '[FEATURE] '
labels: enhancement, discussion
assignees: ''
body:
  - type: markdown
    attributes:
      value: |
        Have a feature request? Please explain the problem you are trying to solve and describe the proposed solution.
  - type: textarea
    id: problem
    attributes:
      label: Is your feature request related to a problem?
      description: A clear and concise description of what the problem is. (e.g. I'm always frustrated when...)
      placeholder: Describe the problem...
    validations:
      required: true
  - type: textarea
    id: solution
    attributes:
      label: Describe the solution you'd like
      description: A clear and concise description of what you want to happen.
      placeholder: Describe the proposed enhancement...
    validations:
      required: true
  - type: textarea
    id: alternatives
    attributes:
      label: Describe alternatives you've considered
      description: A clear and concise description of any alternative solutions or features you've considered.
      placeholder: List any alternatives...
  - type: textarea
    id: context
    attributes:
      label: Additional context
      description: Add any other context or screenshots about the feature request here.
      placeholder: Optional context...
