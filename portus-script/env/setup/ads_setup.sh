#!/bin/bash

TEST=`echo $1 | grep ^192.168.24.`
if [ "" = "$1" -o "$TEST" != "$1" ]; then
    echo Invalid IP Address
    exit
fi

wget -q http://updates.vmd.citrix.com/XenServer/5.5.0/GPG-KEY -O- | apt-key add -
aptitude update
aptitude -Ry upgrade
aptitude -Ry install ssh ntp
aptitude -y purge linux-image-2.6-686-bigmem linux-image-2.6-xen linux-image-2.6.26-2-686-bigmem

cat << EOF > /etc/network/interfaces
# This file describes the network interfaces available on your system
# and how to activate them. For more information, see interfaces(5).

# The loopback network interface
auto lo eth0
iface lo inet loopback

# The primary network interface
iface eth0 inet static
  address $1
  netmask 255.255.255.0
  gateway 192.168.24.1
EOF

/etc/init.d/networking restart
aptitude -Ry install nis autofs nfs-common sudo

cat << 'EOF' > /etc/sudoers
# /etc/sudoers
#
# This file MUST be edited with the 'visudo' command as root.
#
# See the man page for details on how to write a sudoers file.
#

Defaults	env_reset

# Host alias specification

# User alias specification

# Cmnd alias specification

# User privilege specification
root	ALL=(ALL) ALL

# Uncomment to allow members of group sudo to not need a password
# (Note that later entries override this, so you might need to move
# it further down)
# %sudo ALL=NOPASSWD: ALL

chimera	ALL=(ALL) NOPASSWD: ALL
tatsu	ALL=(ALL) NOPASSWD: ALL
EOF

cat << 'EOF' > /etc/nsswitch.conf
# /etc/nsswitch.conf
#
# Example configuration of GNU Name Service Switch functionality.
# If you have the `glibc-doc-reference' and `info' packages installed, try:
# `info libc "Name Service Switch"' for information about this file.

passwd:         nis compat
group:          nis compat
shadow:         nis compat
automount:      nis compat

hosts:          nis files dns
networks:       files

protocols:      db files
services:       db files
ethers:         db files
rpc:            db files

netgroup:       nis
EOF

/etc/init.d/autofs restart

cat << EOF > /etc/apt/sources.list
deb http://ftp.jp.debian.org/debian/ lenny main contrib non-free
deb-src http://ftp.jp.debian.org/debian/ lenny main contrib non-free

deb http://security.debian.org/ lenny/updates main
deb-src http://security.debian.org/ lenny/updates main

deb http://volatile.debian.org/debian-volatile lenny/volatile main
deb-src http://volatile.debian.org/debian-volatile lenny/volatile main
EOF

aptitude update
aptitude -Ry install sun-java6-jdk
