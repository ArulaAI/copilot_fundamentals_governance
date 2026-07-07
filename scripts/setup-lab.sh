#!/bin/bash

echo "Setting up Copilot Governance Lab for Java..."

if ! command -v java &> /dev/null; then
    echo "ERROR: Java 17+ is not installed. Please install a compatible JDK."
    exit 1
fi

JAVA_MAJOR=$(java -version 2>&1 | awk -F '"' '/version "/{print $2}' | awk -F'.' '{print ($1=="1"?$2:$1)}')
if [ -n "$JAVA_MAJOR" ] && [ "$JAVA_MAJOR" -lt 17 ] 2>/dev/null; then
    echo "ERROR: Java 17+ is required. Found Java $JAVA_MAJOR."
    exit 1
fi

# Verify Maven is installed.
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is required but was not found."
    exit 1
fi

echo "Verifying Maven build..."
mvn -B -q validate
if [ $? -ne 0 ]; then
    echo "ERROR: Maven validation failed."
    exit 1
fi

echo "Setup complete!"
echo ""
echo "Next steps:"
echo "  1. Run 'mvn spring-boot:run' to start the development server"
echo "  2. Run 'mvn test' to execute the unit tests"
echo "  3. Review .github/instructions/java.instructions.md for team guidelines"
echo "  4. Follow the workflow in LAB_ACTION_GUIDE.md"
echo ""
echo "Happy coding!"
