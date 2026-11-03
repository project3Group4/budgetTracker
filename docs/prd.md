# Product Requirements Document (PRD): `Budget Tracker!`

[Back to the Project 03 guide](README.md)

> Copy this file to `docs/prd.md` in your team repo and replace everything in angle brackets.
> Keep it to **one page**. Commit it **before the group proposal presentation**; the commit date is checked.
> After the presentation, change it only through pull requests, so the history shows what changed and why.
> Your Final Submission is graded against this document.

**Team:** `Group 4` · **Repo:** `https://github.com/project3Group4/budgetTracker` · **Last updated:** `11/2/2026`

---

## 1. Users

Who is this for? Name **one** primary user type and describe them in one or two sentences.
"Everyone" and "students" are not user types; "a first-year student looking for a study group for
one specific course" is.

- **Primary user:** `For people who have financial troubles and need help budgeting their expenses.`

## 2. Problem

Two sentences: what the user struggles with today, and why existing options fall short.

`The user struggles knowing how much of their money is going where and existing options fall short because
there is too many options. We restrict where they can put their money towards. It's a simplified version of where they
should be spending their money.`

## 3. Success measures

Three measurable signals that would show the product works. Each one needs a number and a way to
check it.

| # | Measure                                                                    | Target                                                                                          | How we check it                |
|---|----------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|--------------------------------|
| S1 | `user sees exact amount of money they should be saving from their paycheck` | `the percentage the user inputs translated into specific money amount based on paycheck amount` | `manually or through tests`    |
| S2 | `user can see their largest spending category`                             | `largest spending category is correct in all cases`                                             | `manually or through tests`    |
| S3 | `user easily can see spending habits`                                      | `under 15 sec`                                                                                  | `timed during onboarding test` |

## 4. Scope: MVP, vertical slice, and not building

**MVP** is the smallest set of stories that makes *this* product useful. It always includes the
account baseline (sign in with GitHub and Google, update profile, delete account) **plus** the
stories specific to your product.

- **MVP stories:** `PRD-1, PRD-2, PRD-3, PRD-11, PRD-14, PRD-16`
- **Vertical slice:** `Adding an expense with an amount and category, save it to the database, and be displayed on home screen. This feature will be tested end-to-end usign Playwright.`
- **Not building (this term):** `We will not be adding investment tips or passive financial growth`

## 5. User stories and acceptance criteria
One row per MVP story. Each GitHub issue links to its story ID (`PRD-3`) and each story has at
least one acceptance criterion in **Given / When / Then** form that someone outside the team could
test.

| ID     | Story                                                                                                                                                 | Acceptance criteria                                                                                                                                                   |
|--------|-------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| PRD-1  | As a `user`, I want to `sign in with Google or GitHub` so that `I can access my budget information`.                                                  | **Given** `that they have a Google or GitHub account`, **when** `their account is verified to not have been registered yet `, **then** `they go to the landing page`. |
| PRD-2  | As a `user`, I want to `set or update the paycheck` so that `I can track how much money I have to spend`.                                             | **Given** `that the user is signed in`, **when** `they enter and save a valid paycheck amount`, **then** `it's stored and displayed on home screen`                   |
| PRD-3  | As a `user`, I want to `add or update expenses with an amount` so that `I can keep track of my spending`.                                             | **Given** `that the user is signed in`, **when**, `I submit an expense with valid information`, **then** `the expense is saved and appears in my spendings page`      |
| PRD-4  | As a `user`, I want to `be able to delete my account` so that `the app doesn't have my information anymore`.                                          | **Given** `the user is signed in`, **when** `they hit the delete button`, **then** `their account and relating information is deleted`.                               |
| PRD-5  | As a `user`, I want to `see my total expenses compared to the income` so that `I can see if I need to cut back on certain expenses.`                  | **Given** `the user is signed in`, **when** `they navigate to their overall spendings`, **then** `they can see the sum of their expenses vs their income`.            |
| PRD-6  | As a `user`, I want to `be able to delete an expense` so that `if I make an mistake it can be erased from expense`                                    | **Given** `the user is signed in`, **when** `they click delete expense`, **then** `the expense is deleted and their budget will be more accurate`                     |
| PRD-7  | As a `user`, I want to `want to update my display name` so that `my profile shows the name I prefer`                                                  | **Given** `the user is signed in`, **when** `I enter and save a valid display name`, **then** `the new name appears on my profile.`                                                                                                          |
| PRD-8  | As an `admin`, I want to `have the ability to add or remove an admin role` so that `I can make a user an admin or an admin a user`                    | **Given** `the admin is signed in and on the admin page`, **when** `the admin clicks change role`, **then** `the user's role will be updated`                                                                                                                                |
| PRD-9  | As an `admin`, I want to `be able to delete a user` so that `if for some reason a user can't delete their own account, an admin can do so too`        | **Given** `the admin is signed in and on the admin page`, **when** `the admin clicks delete user`, **then** `the user will be deleted`                                                                                                                                |
| PRD-10 | As a `user`, I want to `be able to sign out` so that `I don't need to remain signed in if I share devices or don't want to be logged in`              | **Given** `the user is signed in`, **when** `they click sign out`, **then** `the user will be signed out and send back to the login screen`                                                                                                                                |
| PRD-11 | As a `user`, I want to `set or update my savings percentage` so that `I can change how much I'd want or plan to go into savings for my next paycheck` | **Given** `the user is signed in`, **when** `they navigate to their savings page and update their savings percentage`, **then** `their next inputted check will automatically put that new percentage into savings`                                                                                                                                |                                                                                       | **Given** ``, **when** ``, **then** ``                                                                                                                                |
| PRD-12 | As a `user`, I want to `see how much money I have remaining have expenses and savings` so that `I know how much money I have available to spend`      | **Given** `the user is signed in and has entered their pacheck`, **when** `they navigate to their home screen`, **then** `their remaining balance will be displayed after subtracting their expenses from their paycheck` |
| PRD-13 | As a `user`, I want to `be notified when I enter an invalid expense amount` so that ` so I can correct mistakes and keep the budget accurate`         | **Given** `the user is signed in and adding/updating an expense`, **when** `they enter a negative number or leave it empty`, **then** `an error message will be displayed and the update will not be saved`               |
| PRD-14 | As a `user`, I want to `update my profile information` so that `I can keep my account information accurate`                                           | **Given** `the user is signed in and on their profile page`, **when** `they update thier display name and save the changes`, **then** `their updated name will be saved and displayed on their profile`                   |
| PRD-15 | As a `user`, I want to `choose an expense category from a predefined list` so that `my spending is organized consistently`                            | **Given** `I'm adding or updating an expense`, **when** `i open the category selector`, **then** `I can choose a category provided by the app`                                                                                                                                |
| PRD-16 | As a `user`, I want to `see my largest spending category in my current paycheck budget` so that `I know which category accounts for the most spending`| **Given** `my current paycheck budget has expenses`, **when** `I view my spending summary`, **then** `the category with the highest total is identified`                                                                                                                                |
| PRD-17 | As a `user`, I want to `start a new budget when I enter a new paycheck` so that `I can track my spending for that paycheck`                           | **Given** `I have a current paycheck budget`, **when** `I enter a new paycheck`, **then** `a new paycheck budget is created for it and new expenses can be added to it`                                                                                                                                |
| PRD-18 | As a `user`, I want to `login to an existing account` so that `I can access previous expense/budget information`                                      | **Given** `the user has an account and expenses associated to their budget`, **when** `the user logs back in`, **then** `they should see the expenses they had before logging out`                                                                                                                                |
| PRD-19 | As a `user`, I want to `be able to delete my account and use that same email again` so that `I can add new information to the new account`            | **Given** `the user had deleted their account previously`, **when** `the user makes a new account with that same email/github`, **then** `the user is prompted to put new information into the new account`                                                                                                                                |
| PRD-20 | As a `admin`, I want to `I want to make new spending categories` so that `user's have more options to categorize spendings`                           | **Given** `the user logged in is an admin`, **when** `the admin creates a new expense category`, **then** `all the other users now see the new expense category`                                                                                                                                |

---

### Change log

| Date       | PR   | What changed and why |
|------------|------|---|
| `10/31/26` | `#1` | Initial version |
|`11/2/2026`|`#8`|added more prds|
