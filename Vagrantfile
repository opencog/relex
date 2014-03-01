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
  config.vm.box = "precise64"
  config.vm.box_url = "http://files.vagrantup.com/precise64.box"
  config.vm.hostname = "relexbox"
  config.vm.provision "shell", inline: "sed -i 's/us.archive.ubuntu.com/us.archive.ubuntu.com/g' /etc/apt/sources.list"
  config.vm.provision "shell", inline: "ln -v -s /vagrant /home/vagrant/relex"
  config.vm.provision "shell", inline: "/vagrant/install-scripts/install-ubuntu-dependencies.sh"

  config.vm.provider :virtualbox do |vb|
      vb.name = "relexbox"
      vb.customize [
                   "modifyvm", :id,
                   "--memory", "1024",
                   "--cpus", "1"
                   ]
  end
end
