@getObjectAPI
Feature: Get Objects API

  @api
  Scenario: GET /objects returns a list of valid objects
    When I call api spec "get_objects.yaml" without overrides
    Then response status should be 200
    And response should contain a list of objects
    And response json should match
      | key      | value              |
      | [0].id   | 1                  |
      | [0].name | Google Pixel 6 Pro |

  @api
  Scenario: Get multiple objects by IDs
    When I call api spec "get_objects_by_ids.yaml" with overrides
      | key      | value            |
      | query:id | 3                |
      | query:id | 5                |
      | query:id | ${myId=number:2} |
      | wait     | 10               |
    Then response status should be 200

  @api
  Scenario: Get objects with dynamically generated IDs
    When I call api spec "get_objects_by_ids.yaml" with overrides
      | key      | value                 |
      | query:id | ${randomId1=number:3} |
      | query:id | ${randomId2=number:3} |
      | query:id | ${myId=number:1}      |
    Then response status should be 200
    And response json should match
      | key    | value           |
      | [0].id | ${context:myId} |
