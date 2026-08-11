FROM eclipse-temurin:17-jdk-jammy

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    unzip \
    git \
    git-lfs \
    wget \
    python3 \
    usbutils \
    libx11-6 \
    libx11-xcb1 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libxcomposite1 \
    libxcursor1 \
    libxdamage1 \
    libxfixes3 \
    libxcb1 \
    libxcb-cursor0 \
    libxcb-icccm4 \
    libxcb-image0 \
    libxcb-keysyms1 \
    libxcb-render-util0 \
    libxcb-shape0 \
    libxcb-xinerama0 \
    libxcb-xkb1 \
    libxkbcommon0 \
    libxkbcommon-x11-0 \
    libnss3 \
    libglu1-mesa \
    libpulse0 \
    libasound2 \
    libgl1 \
    && rm -rf /var/lib/apt/lists/*

ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV ANDROID_HOME=/opt/android-sdk
ENV HOME=/root
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false"

ENV PATH=${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${ANDROID_SDK_ROOT}/emulator:$PATH

ARG CMDLINE_TOOLS_VERSION=11076708

RUN mkdir -p "${ANDROID_SDK_ROOT}/cmdline-tools" \
    && curl -fsSL \
    "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip" \
    -o /tmp/cmdline-tools.zip \
    && unzip -q /tmp/cmdline-tools.zip -d /tmp/cmdline \
    && mv /tmp/cmdline/cmdline-tools "${ANDROID_SDK_ROOT}/cmdline-tools/latest" \
    && rm -rf /tmp/cmdline-tools.zip /tmp/cmdline

RUN yes | sdkmanager --licenses

RUN git lfs install

RUN sdkmanager \
    "platform-tools" \
    "platforms;android-34" \
    "build-tools;34.0.0" \
    "emulator" \
    "system-images;android-34;google_apis;x86_64"

RUN echo "no" | avdmanager create avd \
    -n Pixel_API_34 \
    -k "system-images;android-34;google_apis;x86_64" \
    --force

WORKDIR /project

COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY build.gradle.kts .
RUN mkdir -p app
COPY app/build.gradle.kts app/build.gradle.kts
COPY gradle/libs.versions.toml gradle/libs.versions.toml

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon --quiet || true

COPY . .

RUN sed -i 's/\r$//' gradlew run.sh \
    && chmod +x gradlew run.sh

RUN echo "sdk.dir=${ANDROID_SDK_ROOT}" > local.properties

COPY run.sh /run.sh
RUN chmod +x /run.sh

CMD ["/run.sh"]