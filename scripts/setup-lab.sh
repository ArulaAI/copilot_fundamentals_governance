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

# Warn if the system Maven is 4.x — the project is pinned to 3.9.x via ./mvnw.
if command -v mvn &> /dev/null; then
    MVN_MAJOR=$(mvn -version 2>&1 | awk '/Apache Maven/{print $3}' | cut -d. -f1)
    if [ "$MVN_MAJOR" -ge 4 ] 2>/dev/null; then
        echo "WARNING: Maven $MVN_MAJOR.x detected. This project is tested against Maven 3.9.x."
        echo "         Use './mvnw' instead of 'mvn' to ensure a compatible build."
    fi
fi

echo "Verifying Maven build via wrapper (Maven 3.9.9)..."
./mvnw -B -q validate
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
