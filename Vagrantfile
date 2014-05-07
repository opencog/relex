# -*- mode: ruby -*-
# # vi: set ft=ruby :

# QuickStart - Vagrantfile for RelEx - Get Vagrant at http://www.vagrantup.com/
# 1. vagrant up
# 2. vagrant ssh
# 3. cd relex && ant run
# Optional
# 1. Change Ubuntu archive mirror
# 2. Change memory and cpus values
# 3. Change to provider vagrant-lxc on Linux

Vagrant.configure("2") do |config|
  config.vm.box = "trusty64"
  config.vm.box_url = "http://files.vagrantup.com/trusty64.box"
  config.vm.box_url = "https://cloud-images.ubuntu.com/vagrant/trusty/current/trusty-server-cloudimg-amd64-vagrant-disk1.box"
  config.vm.hostname = "relexbox"
  config.vm.provision "shell", inline: "sed -i 's/us.archive.ubuntu.com/us.archive.ubuntu.com/g' /etc/apt/sources.list"
  config.vm.provision "shell", inline: "ln -v -s /vagrant /home/vagrant/relex"
  config.vm.provision "shell", inline: "/vagrant/install-scripts/install-ubuntu-dependencies.sh"

  # For working with OpenCog, change the relex-server-host variable value
  # from "127.0.0.1" to 192.168.50.3 in  "opencog/scm/config.scm" file, or
  # run (define relex-server-host "192.168.50.3") from the scheme shell of
  # the cogserver.
  config.vm.network "private_network", ip: "192.168.50.3"

  config.vm.provider :virtualbox do |vb|
      vb.name = "relexbox"
      vb.customize [
                   "modifyvm", :id,
                   "--memory", "1024",
                   "--cpus", "1"
                   ]
  end
end
