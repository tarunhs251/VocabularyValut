# Contributing to VocabVault

Thank you for your interest in contributing to VocabVault! This document provides guidelines and instructions for contributing to the project.

## Code of Conduct

Please be respectful and constructive in all interactions. We're building a welcoming community for everyone.

## How to Contribute

### Reporting Bugs

If you find a bug, please create an issue with:
- A clear, descriptive title
- A detailed description of the bug
- Steps to reproduce the problem
- Expected behavior vs. actual behavior
- Your environment (Android version, device, Android Studio version)
- Screenshots or logs if applicable

### Suggesting Enhancements

For feature requests:
- Use a clear, descriptive title
- Provide a detailed description of the suggested enhancement
- Explain why this enhancement would be useful
- List any similar features in other apps if applicable

### Pull Requests

1. **Fork the Repository**
   ```bash
   git clone https://github.com/yourusername/VocabVault.git
   cd VocabVault
   ```

2. **Create a Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make Your Changes**
   - Follow the existing code style and architecture
   - Write clear, concise commit messages
   - Include comments for complex logic
   - Test your changes thoroughly

4. **Commit Your Changes**
   ```bash
   git commit -m "Add feature: brief description"
   ```

5. **Push to Your Fork**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Create a Pull Request**
   - Provide a clear description of your changes
   - Reference any related issues
   - Include screenshots/videos if UI changes
   - Ensure all tests pass

## Code Style Guidelines

### Kotlin/Java
- Use 4-space indentation
- Follow [Google's Kotlin style guide](https://kotlinlang.org/docs/reference/coding-conventions.html)
- Use meaningful variable and function names
- Keep functions focused and concise
- Add KDoc comments for public APIs

### Compose
- Keep composables focused on a single responsibility
- Use descriptive names for composables
- Hoist state appropriately
- Use `@Preview` annotations for testing UI

### Git Commits
- Write clear, descriptive commit messages
- Use imperative mood: "Add feature" not "Added feature"
- Keep commits atomic and focused
- Reference issues when applicable: "Fixes #123"

## Testing

- Write unit tests for new logic
- Test UI changes manually on multiple devices/Android versions
- Ensure all existing tests still pass
- Aim for meaningful test coverage

## Development Setup

1. Clone and open in Android Studio
2. Allow Gradle to sync and download dependencies
3. Run `./gradlew build` to ensure everything compiles
4. Run tests with `./gradlew test`

## Questions?

- Check existing issues and discussions
- Create a new discussion if your question isn't covered
- Reach out in the GitHub community

Thank you for helping make VocabVault better! 🎉
