# From https://www.scala-sbt.org/download/
echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | sudo tee /etc/apt/sources.list.d/sbt.list
echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | sudo tee /etc/apt/sources.list.d/sbt_old.list
curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | sudo apt-key add
sudo apt-get update
sudo apt-get install sbt

# From https://www.digitalocean.com/community/tutorials/how-to-install-java-with-apt-on-debian-11
sudo apt install default-jre -y
sudo apt install default-jdk -y

javac -version
java -version
sbt --version
