#!groovy
def jenkinsLTS = "2.89.4"
buildPlugin(configurations: [
  [ platform: "linux", jdk: "8", jenkins: jenkinsLTS, javaLevel: "8" ],
  [ platform: "windows", jdk: "8", jenkins: jenkinsLTS, javaLevel: "8" ],
  [ platform: "linux", jdk: "11", jenkins: jenkinsLTS, javaLevel: "8" ],
])
