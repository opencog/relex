# QuickStart - Vagrantfile for RelEx - Get Vagrant at http://www.vagrantup.com/
# 1. vagrant box add precise64 http://files.vagrantup.com/precise64.box
# 2. vagrant up
# 3. vagrant ssh
# Optional
# 1. Change Ubuntu archive mirror
# 2. Change memory and cpus values
# 3. Change to provider vagrant-lxc on Linux

Vagrant.configure("2") do |config|
  config.vm.box = "precise64"
  config.vm.hostname = "relexbox"
  config.vm.provision "shell", inline: "sed -i 's/us.archive.ubuntu.com/au.archive.ubuntu.com/g' /etc/apt/sources.list"
  config.vm.provision "shell", inline: "ln -v -s /vagrant /home/vagrant/relex"
  config.vm.provision "shell", inline: "/vagrant/install-scripts/install-ubuntu-dependencies.sh"

  config.vm.provider :virtualbox do |vb|
      vb.name = "relexbox"
      vb.customize [
                   "modifyvm", :id,
                   "--memory", "1024",
                   "--name", "relex-vm",
                   "--cpus", "1"
                   ]
  end
end
