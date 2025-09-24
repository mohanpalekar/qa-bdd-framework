@addObjectsAPI
Feature: Add Objects API

  @api @data
  Scenario: Add Objects API - Positive
    When I call api spec "add_objects.yaml" with overrides
      | key          | value              |
      | $.name       | ${name=firstname}  |
      | $.data.year  | ${year=number:4}   |
      | $.data.price | ${price=float:3:2} |
    Then response status should be 200
    And response json should match
      | key        | value            |
      | name       | ${context:name}  |
      | data.year  | ${context:year}  |
      | data.price | ${context:price} |
      | id         | __notempty__     |
      | createdAt  | __exists__       |

  @api
  Scenario: Add Objects API - Positive - 1
    When I call api spec "add_objects.yaml" with overrides
      | key          | value              |
      | $.name       | ${name=firstname}  |
      | $.data.year  | ${year=number:4}   |
      | $.data.price | ${price=float:3:2} |
    Then response status should be 200
    And response json should match
      | key        | value            |
      | name       | ${context:name}  |
      | data.year  | ${context:year}  |
      | data.price | ${context:price} |
      | id         | __notempty__     |
      | createdAt  | __exists__       |
