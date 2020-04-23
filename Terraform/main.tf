# Configure the Microsoft Azure Provider
provider "azurerm" {
    version = "~>2.0"
    features {}
}
terraform {
  backend "azurerm" {
    storage_account_name  = "insightsinfra"
    container_name        = "terraform"
    key                   = "terraform/eastus/terraform.tfstate"
    access_key            = "+Ur+CFdxDmzgsQFVlBgqRdR8aOibP1ADq135pbiAtoQWkfmNliKfiHZ70jiy+qDmEA6jf8953Zn16krUiNtC6A=="
  }
}
data "azurerm_subscription" "primary" {
}

# Create a resource group if it doesn't exist
resource "azurerm_resource_group" "Resource_Group" {
    name     = "InsightResourceGroup"
    location = "${var.location}"

    tags = {
        environment = "Terraform Demo"
    }
}

# Create virtual network
resource "azurerm_virtual_network" "network" {
    name                = "${var.name}"
    address_space       = ["10.0.0.0/16"]
    location            = "${var.location}"
    resource_group_name = "${azurerm_resource_group.Resource_Group.name}"

    tags = {
        environment = "Terraform Demo"
    }
}

# Create subnet
resource "azurerm_subnet" "subnet" {
    name                 = "${var.name}-Subnet"
    resource_group_name  = "${azurerm_resource_group.Resource_Group.name}"
    virtual_network_name = "${azurerm_virtual_network.network.name}"
    address_prefix       = "10.0.1.0/24"
}

# Create public IPs
resource "azurerm_public_ip" "publicip" {
    name                         = "${var.name}-publicIP"
    location                     = "${var.location}"
    resource_group_name          = "${azurerm_resource_group.Resource_Group.name}"
    allocation_method            = "Dynamic"

    tags = {
        environment = "Terraform Demo"
    }
}

# Create Network Security Group and rule
resource "azurerm_network_security_group" "nsg" {
    name                = "${var.name}-SecurityGroup"
    location            = "${var.location}"
    resource_group_name = "${azurerm_resource_group.Resource_Group.name}"
    
    security_rule {
        name                       = "SSH"
        priority                   = 1001
        direction                  = "Inbound"
        access                     = "Allow"
        protocol                   = "Tcp"
        source_port_range          = "*"
        destination_port_range     = "22"
        source_address_prefix      = "*"
        destination_address_prefix = "*"
    }

    tags = {
        environment = "Terraform Demo"
    }
}

# Create network interface
resource "azurerm_network_interface" "nic" {
    name                      = "${var.name}-NIC"
    location                  = "${var.location}"
    resource_group_name       = "${azurerm_resource_group.Resource_Group.name}"

    ip_configuration {
        name                          = "${var.name}-NicConfiguration"
        subnet_id                     = "${azurerm_subnet.subnet.id}"
        private_ip_address_allocation = "Dynamic"
        public_ip_address_id          = "${azurerm_public_ip.publicip.id}"
    }

    tags = {
        environment = "Terraform Demo"
    }
}

# Connect the security group to the network interface
resource "azurerm_network_interface_security_group_association" "example" {
    network_interface_id      = "${azurerm_network_interface.nic.id}"
    network_security_group_id = "${azurerm_network_security_group.nsg.id}"
}

# Generate random text for a unique storage account name
resource "random_id" "randomId" {
    keepers = {
        # Generate a new ID only when a new resource group is defined
        resource_group = "${azurerm_resource_group.Resource_Group.name}"
    }
    
    byte_length = 8
}

# Create storage account for boot diagnostics
resource "azurerm_storage_account" "storageaccount" {
    name                        = "diag${random_id.randomId.hex}"
    resource_group_name         = "${azurerm_resource_group.Resource_Group.name}"
    location                    = "${var.location}"
    account_tier                = "Standard"
    account_replication_type    = "LRS"

    tags = {
        environment = "Terraform Demo"
    }
}

# Create virtual machine
resource "azurerm_linux_virtual_machine" "bastion" {
    name                  = "${var.name}-VM"
    location              = "${var.location}"
    resource_group_name   = "${azurerm_resource_group.Resource_Group.name}"
    network_interface_ids = ["${azurerm_network_interface.nic.id}"]
    size                  = "Standard_D2s_v3"
    custom_data           = "${base64encode(data.template_file.cloudconfig.rendered)}"

    os_disk {
        name              = "${var.name}-OsDisk"
        caching           = "ReadWrite"
        storage_account_type = "Standard_LRS"
    }

    source_image_reference {
        publisher = "Canonical"
        offer     = "UbuntuServer"
        sku       = "16.04.0-LTS"
        version   = "latest"
    }

    computer_name  = "bastion"
    admin_username = "ubuntu"
    disable_password_authentication = true
        
    admin_ssh_key {
        username       = "ubuntu"
        public_key     = "${file("./id_rsa.pub")}"
    }
    provisioner "remote-exec" {
        inline = [
            "mkdir /home/ubuntu/.kube",
        ]
        connection {
            type        = "ssh"
            host        = "${azurerm_linux_virtual_machine.bastion.public_ip_address}"
            user        = "ubuntu"
            private_key = "${file("./id_rsa")}"
        }
    }
    provisioner "file" {
        source      = "kube_config"
        destination = "/home/ubuntu/.kube/config"

        connection {
            type        = "ssh"
            host        = "${azurerm_linux_virtual_machine.bastion.public_ip_address}"
            user        = "ubuntu"
            private_key = "${file("./id_rsa")}"
        }
    }

    identity {
        type = "SystemAssigned"
    }

    boot_diagnostics {
        storage_account_uri = "${azurerm_storage_account.storageaccount.primary_blob_endpoint}"
    }

    tags = {
        environment = "Terraform Demo"
    }
    depends_on = [local_file.kube_config]
}

data "template_file" "cloudconfig" {
  template = "${file("user_data.sh")}"
}

resource "azurerm_role_definition" "admin_vm" {
  name        = "admin-vm-role"
  scope       = "${data.azurerm_subscription.primary.id}"
  description = "This is a custom role created via Terraform"

  permissions {
    actions     = ["*"]
    not_actions = []
  }

  assignable_scopes = [
    "${data.azurerm_subscription.primary.id}", 
  ]
}
resource "azurerm_role_assignment" "bastion" {
    scope              = "${data.azurerm_subscription.primary.id}"
    role_definition_id = "${data.azurerm_subscription.primary.id}${azurerm_role_definition.admin_vm.id}"
    principal_id       = "${azurerm_linux_virtual_machine.bastion.identity[0].principal_id}"

    lifecycle {
        ignore_changes = [
            role_definition_id,
        ]
    }
}

############################## ECR ######################################

provider "azuread" {
  version = "~> 0.3"
}
resource "azurerm_container_registry" "acr" {
  name                = "${var.name}acr768"
  resource_group_name = "${azurerm_resource_group.Resource_Group.name}"
  location            = "${azurerm_resource_group.Resource_Group.location}"
  sku                 = "standard"
}

resource "azuread_application" "acr-app" {
  name = "${var.name}app768"
}

resource "azuread_service_principal" "acr-sp" {
    application_id = "${azuread_application.acr-app.application_id}"
}

resource "azuread_service_principal_password" "acr-sp-pass" {
  service_principal_id = "${azuread_service_principal.acr-sp.id}"
  value                = "Password123"
  end_date             = "2021-01-01T01:02:03Z"
}

resource "azurerm_role_assignment" "acr-assignment" {
    scope                = "${azurerm_container_registry.acr.id}"
    role_definition_name = "Contributor"
    principal_id         = "${azuread_service_principal_password.acr-sp-pass.service_principal_id}"
}

########################## AKS #############################################

resource "azurerm_kubernetes_cluster" "aks" {
    name                = "${var.cluster_name}"
    location            = "${azurerm_resource_group.Resource_Group.location}"
    resource_group_name = "${azurerm_resource_group.Resource_Group.name}"
    dns_prefix          = "${var.dns_prefix}"

    linux_profile {
        admin_username = "ubuntu"

        ssh_key {
            key_data = "${file("./id_rsa.pub")}"
        }
    }

    default_node_pool {
        name            = "agentpool"
        vm_size         = "Standard_D2s_v3"
        enable_auto_scaling = true
        max_count       = 5
        min_count       = 1
        node_count      = 1
        os_disk_size_gb = 30
    }

    identity {
        type = "SystemAssigned"
    }

    tags = {
        environment = "Terraform Demo"
    }
}
resource "local_file" "kube_config" {
    content     = "${azurerm_kubernetes_cluster.aks.kube_config_raw}"
    filename    = "kube_config"

    depends_on = [azurerm_kubernetes_cluster.aks]
}

output "docker" {
  value = "docker login ${azurerm_container_registry.acr.login_server} -u ${azuread_service_principal.acr-sp.application_id} -p ${azuread_service_principal_password.acr-sp-pass.value}"
}

output "client_certificate" {
  value = "${azurerm_kubernetes_cluster.aks.kube_config.0.client_certificate}"
}

output "kube_config" {
  value = "${azurerm_kubernetes_cluster.aks.kube_config_raw}"
}