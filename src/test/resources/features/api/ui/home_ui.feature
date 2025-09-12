Feature: DemoQA UI interactions

  @ui @ui1
  Scenario: Verify Login Success
    Given I launch browser "browser"
    Given I navigate to URL "sit.url"
    When I perform UI actions
      | operation   | locatorKey                        | value                             |
      | type        | login.username                    | Admin                             |
      | type        | login.password                    | admin123                          |
      | keyboard    |                                   | enter                             |
#      | wait       |                                   | 2                                 |
#      | mouseOver  | login.submit                      |                                   |
#      | click      | login.forgot.password             |                                   |
      | wait        |                                   | 2                                 |
      | verify text | home.dashboard                    | Dashboard                         |
      | verify text | home.employee.distribution.report | Employee Distribution by Location |
      | click       | home.user.menu                    |                                   |
      | wait        |                                   | 2                                 |
#      | verifyText | home.logout     | Logout    |
      | keyboard    |                                   | tab                               |
      | keyboard    |                                   | tab                               |
      | keyboard    |                                   | tab                               |
      | keyboard    |                                   | tab                               |
      | keyboard    |                                   | enter                             |
      | verify text | login.page.text                   | Login                             |

  @ui
  Scenario: Verify Login Failure
    Given I launch browser "browser"
    Given I navigate to URL "sit.url"
    When I perform UI actions
      | operation   | locatorKey     | value               |
      | type        | login.username | ${userEmail=email}  |
      | type        | login.password | ${number:5}         |
      | keyboard    |                | enter               |
      | wait        |                | 2                   |
      | verify text | login.failed   | Invalid credentials |

#    When I perform UI actions
#      | operation   | locatorKey       | value              |
#      | type        | login.username   | Admin              |
#      | type        | login.password   | admin123           |
#      | type        | form.emailField  | ${userEmail=email} |
#      | click       | login.button     |                    |
#      | mouseOver   | home.profileIcon |                    |
#      | doubleClick | home.quickAction |                    |
#      | verifyText  | login.welcome    | Welcome Admin      |
#
#
#    When I perform UI actions
#      | operation  | locatorKey     | value                       |
#      | type       | login.username | ${user1=firstname+number:3} |
#      | type       | login.password | ${pass1=string:8}           |
#      | type       | login.email    | ${email=email}              |
#      | click      | login.submit   |                             |
#      | verifyText | login.welcome  | Welcome ${context:user1}    |



