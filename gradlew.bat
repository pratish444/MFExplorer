@rem
@rem Copyright 2015 the original author or authors.
@rem Licensed under the Apache License, Version 2.0
@rem

@if "%DEBUG%"=="" @echo off

set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

if "%JAVA_HOME%"=="" (
    set JAVA_EXE=java
) else (
    set JAVA_EXE=%JAVA_HOME%/bin/java
)

set DIRNAME=%~dp0
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
