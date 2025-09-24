Feature: User API

  @api @createUser
  Scenario: Create User API - Positive Scenario
    When I call api spec "user-api.yaml" with overrides
      | key           | value            |
      | path:tenantId | 12345            |
      | query:verbose | true             |
      | $.name        | John Doe         |
      | $.email       | john@example.com |
    Then response status should be 404
    And response json should match
      | key    | value     |
      | status | 404       |
      | error  | Not Found |
