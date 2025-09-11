Feature: User API

  @api
  Scenario: Create User API - Positive Scenario
    When I call api spec "user-api.yaml" with overrides
      | key           | value            |
      | path:tenantId | 12345            |
      | query:verbose | true             |
      | $.name        | John Doe         |
      | $.email       | john@example.com |
    Then response status should be 201
    And response json should match
      | key    | value    |
      | $.name | John Doe |
