### Title:
Add proper error handling and user feedback

### Description:
## Issue Description
From IMPROVEMENTS.md section 1.2 - Error Handling Enhancement

### Current State
Error handling is minimal with silent failures in resource collection.

### Required Improvements
- Add proper error UI display for failed operations
- Implement retry logic for failed network operations
- Add user-friendly error messages
- Log errors properly for debugging
- Handle edge cases (empty list, invalid input)

### Priority
HIGH - Core Functionality

### Acceptance Criteria
- [ ] Error UI component created and displayed on error states
- [ ] Retry mechanism implemented for network operations
- [ ] User-friendly error messages defined for common scenarios
- [ ] Error logging added throughout the application
- [ ] Edge cases documented and handled