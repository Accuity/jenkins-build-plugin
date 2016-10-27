# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|

  # Use hostname instead of fqdn so vagrant commands are easier to type
  hostname   = 'slaver'
  domain     = 'ex.ds'
  fqdn       = "#{hostname}.#{domain}"
  ipaddress  = '192.168.10.14'
  ports      = [8080]
  jenkins_ip = find_host_ip ipaddress

  user    = 'jenkins-slave'
  group   = user
  home    = "/home/#{user}"
  pub_key = get_public_key

  config.vm.box = 'puppetlabs/centos-7.0-64-puppet'
  config.vm.define hostname do |node|

    node.extend(ExtensionMethods)

    node.vm.hostname = fqdn
    node.vm.network :private_network, ip: ipaddress

    node.vm.provider :virtualbox do |vb|
      vb.customize ['modifyvm', :id, '--name', fqdn]
    end

    node.vm.synced_folder './slave', '/opt/host'

    node.open_ports ports
    node.host_entry jenkins_ip, 'jenkins.ex.ds', 'jenkins'

    # openjdk-1.7.0.111 breaks slave connection
    node.puppet_command 'resource package java-1.7.0-openjdk-1.7.0.99 ensure=installed allow_virtual=true'
    node.puppet_command "resource group #{group} ensure='present'"
    node.puppet_command "resource user #{user} groups='#{group}' home='#{home}' managehome='true' ensure='present'"
    node.puppet_command "resource file #{home}/.ssh owner='#{user}' group='#{group}' mode='0600' ensure='directory'"
    node.puppet_command "resource file #{home}/.ssh/authorized_keys owner='#{user}' group='#{group}' mode='0600' content='#{pub_key}'"
  end
end

module ExtensionMethods
  def provision_command(command)
    self.vm.provision :shell, inline: command
  end

  def provision_commands(commands)
    provision_command commands.join("\n")
  end

  def puppet_command(command)
    provision_command "sudo puppet #{command}"
  end

  def host_entry(ip, fqdn, host_alias = nil)
    host_command = "resource host #{fqdn} ip='#{ip}'"
    host_command << " host_aliases='#{host_alias}'" unless host_alias.nil?
    puppet_command host_command
  end

  def open_ports(ports)
    commands = ports.collect { |port|
      "sudo firewall-cmd --zone=public --add-port=#{port}/tcp --permanent"
    }
    commands << 'sudo firewall-cmd --reload' unless ports.empty?
    provision_commands commands
  end
end

def find_host_ip(guest_ip)
  sections = guest_ip.split '.'
  sections[3] = 1
  sections.join '.'
end

def get_public_key
  path = File.expand_path('../keys/ssh.pub', __FILE__)
  abort "Could not find key #{path}" unless File.exists? path
  File.read path
end
