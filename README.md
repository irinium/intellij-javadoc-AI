# IntelliJ JavaDoc AI Plugin

The IntelliJ JavaDoc AI Plugin is a powerful tool that utilizes artificial intelligence to generate JavaDoc comments for your Java code. It leverages the OpenAI GPT-3 API to provide intelligent and context-aware suggestions for documenting your methods, classes, and interfaces.

## Features

- Automatic JavaDoc generation: Simplify the process of writing JavaDoc comments by letting the plugin generate them for you.
- AI-powered suggestions: Benefit from the advanced natural language processing capabilities of OpenAI GPT-3 to receive accurate and relevant JavaDoc comment recommendations.
- Context-aware suggestions: The plugin analyzes your code and takes into account the surrounding context to provide more precise and meaningful JavaDoc suggestions.
- Code navigation: Seamlessly navigate between classes and interfaces, allowing you to quickly review and generate JavaDoc comments for different code elements.

## Installation

1. Launch IntelliJ IDEA.
2. Go to **Settings** (Preferences on macOS) -> **Plugins**.
3. Click on the **Marketplace** tab.
4. Search for "Intellij-javadoc-AI" in the search bar.
5. Click **Install** next to the IntelliJ JavaDoc AI Plugin.
6. Restart IntelliJ IDEA to activate the plugin.

## Usage

1. Open your Java project in IntelliJ IDEA.
2. Navigate to the class or interface for which you want to generate JavaDoc comments.
3. Press **Ctrl + Shift + Alt + G** to trigger the JavaDoc AI Plugin.
4. Optionally, modify the documentation or add additional details as needed.

## Configuration

The plugin requires an API key from OpenAI to access the GPT-3 API. Follow these steps to configure the API key:

1. Sign up for an account on the OpenAI website.
2. Obtain an API key from OpenAI.
3. Set the API key as an environment variable named `OPENAI_API_KEY`.
    - On Linux/macOS, open a terminal and run the following command:
      ```shell
      export OPENAI_API_KEY=YOUR_API_KEY
      ```
    - On Windows, open a command prompt and run the following command:
      ```shell
      set OPENAI_API_KEY=YOUR_API_KEY
      ```
4. Restart IntelliJ IDEA to activate the plugin.

By using an environment variable, you can securely store and manage your API key without exposing it directly in the plugin's configuration files.

**Note: As of now, the free version of ChatGPT does not offer API access**. API usage is billed separately and is not available for free. To use the APIs and plugins, you'll need to set up a subscription.
If you do not have an active subscription, using `YOUR_API_KEY` will result in an error when making API requests, specifically a "429 Too Many Requests" error.
Pricing details for the ChatGPT API are available on the [OpenAI Pricing page](https://openai.com/pricing).

If you encounter any issues or need further assistance with the configuration, please refer to the OpenAI documentation or consult their support resources.


## Limitations

- The plugin relies on the availability and performance of the OpenAI GPT-3 API. Network connectivity and API rate limits may impact its functionality.
- The accuracy and quality of the generated JavaDoc comments are dependent on the capabilities and limitations of the underlying AI model.

## Contributing

Contributions to the IntelliJ JavaDoc AI Plugin are welcome! If you encounter any bugs, have feature requests, or would like to contribute code improvements, please submit an issue or pull request on the GitHub repository.

Before contributing, please review the [contribution guidelines](CONTRIBUTING.md) or send an email to iryna.shvets.dev@gmail.com.

## Author

Iryna Shvets iryna.shvets.dev@gmail.com
