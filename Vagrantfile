# -*- mode: ruby -*-
# vi: set ft=ruby :

$script = <<SCRIPT

 # Exit on any errors.
 set -e

 # Install prerequisites for mesos.
 echo "Installing prerequisite packages for mesos..."
 apt-get -y update
 apt-get -y install openjdk-6-jdk
 apt-get -y install libcurl3
 apt-get -y install zookeeperd

 # Download mesos.
 wget http://downloads.mesosphere.io/master/ubuntu/14.04/mesos_0.20.1-1.0.ubuntu1404_amd64.deb

 echo "Installing mesos..."
 dpkg --install mesos_0.20.1-1.0.ubuntu1404_amd64.deb
 echo "Done"

 # Symlink /usr/lib/libjvm.so for mesos.
 ln -s /usr/lib/jvm/java-6-openjdk-amd64/jre/lib/amd64/server/libjvm.so /usr/lib/libjvm.so

 echo "Starting mesos master"
 start mesos-master

 echo "Starting mesos slave"
 start mesos-slave
SCRIPT


# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  # All Vagrant configuration is done here. The most common configuration
  # options are documented and commented below. For a complete reference,
  # please see the online documentation at vagrantup.com.

  # Every Vagrant virtual environment requires a box to build off of.
  config.vm.box = "trusty64"

  # The url from where the 'config.vm.box' box will be fetched if it
  # doesn't already exist on the user's system.
  config.vm.box_url = "https://oss-binaries.phusionpassenger.com/vagrant/boxes/latest/ubuntu-14.04-amd64-vbox.box"

  # Forward mesos ports.
  config.vm.network "forwarded_port", guest: 5050, host: 5050
  config.vm.network "forwarded_port", guest: 5051, host: 5051

  # Provision the system.
  config.vm.provision "shell", inline: $script

  config.vm.provider :virtualbox do |vb|
     # Use VBoxManage to customize the VM. For example to change memory:
     vb.customize ["modifyvm", :id, "--memory", "2048"]
     vb.customize ["modifyvm", :id, "--cpus", "2"]
  end
end
