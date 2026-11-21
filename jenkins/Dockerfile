# Jenkins with its plugins, configuration and the job baked in (Configuration as Code), plus the Docker CLI
# so pipeline steps can run each build in the right toolchain container.
FROM jenkins/jenkins:lts-jdk21
USER root
COPY --from=docker:27-cli /usr/local/bin/docker /usr/local/bin/docker
RUN git config --system --add safe.directory '*'
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt
COPY casc.yaml /var/jenkins_conf/casc.yaml
ENV CASC_JENKINS_CONFIG=/var/jenkins_conf/casc.yaml \
    JAVA_OPTS="-Djenkins.install.runSetupWizard=false -Dhudson.plugins.git.GitSCM.ALLOW_LOCAL_CHECKOUT=true"
