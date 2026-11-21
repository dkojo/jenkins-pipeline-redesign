// The shared library: every service is built, tested and packaged the same way. Only the toolchain differs.
// Each step runs in its toolchain's container, so the Jenkins machine needs nothing but Docker.

def toolchains() {
  [
    dotnet: [image: 'mcr.microsoft.com/dotnet/sdk:10.0', cache: 'nuget-cache:/root/.nuget',
             build: 'dotnet build Catalog -c Release',
             test: 'dotnet test Catalog.Tests -c Release',
             pack: 'dotnet publish Catalog -c Release -o out'],
    java:   [image: 'maven:3.9-eclipse-temurin-21', cache: 'maven-cache:/root/.m2',
             build: 'mvn -B -q compile',
             test: 'mvn -B -q test',
             pack: 'mvn -B -q package -DskipTests'],
    node:   [image: 'node:24-alpine', cache: 'npm-cache:/root/.npm',
             build: 'npm install --no-audit --no-fund',
             test: 'npm test',
             pack: 'npm pack --silent'],
  ]
}

// Run a command for one service inside its toolchain container, sharing Jenkins' workspace and a dependency cache
def inToolchain(Map s, String what) {
  def tc = toolchains()[s.lang]
  sh "docker run --rm --volumes-from jenkins -v ${tc.cache} -w ${env.WORKSPACE}/${s.dir} ${tc.image} sh -c '${tc[what]}'"
}

// A named stage that also prints a marker the GitHub Actions follower watches for
def step(String name, Closure body) {
  stage(name) {
    body()
    echo "STAGE-OK ${name}"
  }
}

def build(Map s) { inToolchain(s, 'build') }
def test(Map s)  { inToolchain(s, 'test') }
def pack(Map s)  { inToolchain(s, 'pack') }

// Deploy all three behind NGINX: .NET on Kestrel, Java on Tomcat, Node.js on Node, NGINX in front
def deploy() {
  sh """
    docker rm -f catalog orders cart gateway >/dev/null 2>&1 || true
    docker run -d --name catalog --network apps --volumes-from jenkins -w ${env.WORKSPACE}/services/catalog/out \
      -e ASPNETCORE_URLS=http://+:8080 mcr.microsoft.com/dotnet/aspnet:10.0 dotnet Catalog.dll
    docker run -d --name orders --network apps tomcat:10.1-jre21
    docker cp services/orders/target/orders.war orders:/usr/local/tomcat/webapps/orders.war
    docker run -d --name cart --network apps --volumes-from jenkins -w ${env.WORKSPACE}/services/cart node:24-alpine node server.js
    docker run -d --name gateway --network apps -p 8088:80 nginx:1.30.7-alpine
    docker cp gateway/default.conf gateway:/etc/nginx/conf.d/default.conf
    docker exec gateway nginx -s reload
  """
}

// Call every service through NGINX until it answers, or give up after 60 seconds
def smoke() {
  sh """
    for path in catalog/health orders/health cart/health; do
      for i in \$(seq 1 60); do
        curl -sf http://gateway/\$path >/dev/null && echo "OK  /\$path" && break
        [ \$i = 60 ] && { echo "FAIL /\$path"; exit 1; }
        sleep 1
      done
    done
    echo "catalog: \$(curl -s http://gateway/catalog/products)"
    echo "orders:  \$(curl -s http://gateway/orders/list)"
    echo "cart:    \$(curl -s http://gateway/cart/items)"
  """
}

// Record what was released, and keep the artifacts with the build
def release(List services) {
  writeFile file: 'release.json', text: groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(
    [build: env.BUILD_NUMBER, services: services.collect { [name: it.name, lang: it.lang] }]))
  archiveArtifacts artifacts: 'release.json, services/orders/target/orders.war', fingerprint: true
  echo "Released build ${env.BUILD_NUMBER}: ${services.collect { it.name }.join(', ')}"
}
