node {
    stage name:'Checkout'
    checkout scm

    stage name: 'Build', concurrency: 1
    sh "mvn --batch-mode -V -U -e clean verify -DskipITs -Dsurefire.useFile=false"
}