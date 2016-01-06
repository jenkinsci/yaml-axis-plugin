# Jenkins Yaml Axis Plugin
Matrix project axis creation plugin using yaml file

[![Build Status](https://jenkins.ci.cloudbees.com/buildStatus/icon?job=plugins/yaml-axis-plugin)](https://jenkins.ci.cloudbees.com/job/plugins/job/yaml-axis-plugin/)

* https://wiki.jenkins-ci.org/display/JENKINS/Yaml+Axis+Plugin
* https://github.com/jenkinsci/yaml-axis-plugin

## Usage
### 1. Add yaml file to repository
example

```yaml
# axis.yml
RUBY_VERSION:
  - 2.1.8
  - 2.2.4
  - 2.3.0

DATABASE:
  - mysql
  - postgres
  - oracle
```

### 2. Create Multi-configuration project
![new_job](doc/new_job.png)

### 3. Configuration Matrix
Choose **Yaml Axis**

![Add axis](doc/add_axis.png)

Input configurations

![config](doc/config.png)

* **Axis yaml file** : Yaml file path (relative path from workspace or absolute path)
* **Axis name** : Top key in yaml file

### 4. Build job
Generate yaml based matrix and run job :muscle:

![matrix](doc/matrix.png)
