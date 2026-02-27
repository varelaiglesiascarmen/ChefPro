# Copilot Instructions for ChefPro Project

## Communication Preferences

- **Do NOT use emojis** in responses to the user. Keep messages clean and professional without emoji symbols.
- Communication should be clear, direct, and concise.
- Focus on technical accuracy and actionable information.

## Code Style

- Use consistent formatting and follow project conventions
- Write clear comments in Spanish or English as appropriate to the context
- Ensure all changes are well-documented in commit messages

## Error Handling

- Provide clear error messages to the user
- When fixing issues, explain the root cause and the solution implemented
- Include console logs and debugging information when helpful

## Code Comments

- Add concise comments in ENGLISH explaining the functional purpose or business logic
- Strictly forbidden AI tutorial style: no step-by-step explanations or syntax obviousness
- Comment only the "why" and "what for", not the "how"
- Focus on business intent, edge cases, and non-obvious logic

## UI Refactoring Workflow

- Do NOT print large code blocks in chat
- Provide a brief list of key changes made
- Apply changes directly to files using replace_string_in_file tool
- User can review changes directly in VS Code and accept them

## Git Practices

- Write commit messages in English
- Use conventional commits format: `type: description`
- Include detailed description of changes in multi-line commits
