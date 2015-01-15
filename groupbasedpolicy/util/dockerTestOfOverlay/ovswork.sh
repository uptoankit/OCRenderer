#!/bin/bash
set -e

BRIDGE=$1
GUESTNAME=$2
IPADDR=$3
BROADCAST=$4
GWADDR=$5
MAC=$6
OF_PORT=$7
VLANTAG=$8

[ "$IPADDR" ] || {
    echo "Syntax:"
    echo "pipework <hostinterface> <guest> <ipaddr>/<subnet> <broadcast> <gateway> [vlan tag]"
    exit 1
}

# Step 1: Find the guest (for now, we only support LXC containers)
while read dev mnt fstype options dump fsck
do
    [ "$fstype" != "cgroup" ] && continue
    echo $options | grep -qw devices || continue
    CGROUPMNT=$mnt
done < /proc/mounts

[ "$CGROUPMNT" ] || {
    echo "Could not locate cgroup mount point."
    exit 1
}

N=$(find "$CGROUPMNT" -name "$GUESTNAME*" | wc -l)
case "$N" in
    0)
	echo "Could not find any container matching $GUESTNAME."
	exit 1
	;;
    1)
	true
	;;
    *)
	echo "Found more than one container matching $GUESTNAME."
	exit 1
	;;
esac

NSPID=$(head -n 1 $(find "$CGROUPMNT" -name "$GUESTNAME*" | head -n 1)/tasks)
[ "$NSPID" ] || {
    echo "Could not find a process inside container $GUESTNAME."
    exit 1
}

# Step 2: Prepare the working directory
mkdir -p /var/run/netns
rm -f /var/run/netns/$NSPID
ln -s /proc/$NSPID/ns/net /var/run/netns/$NSPID

# Step 3: Creating virtual interfaces
LOCAL_IFNAME=vethl$NSPID
GUEST_IFNAME=vethg$NSPID
ip link add name $LOCAL_IFNAME type veth peer name $GUEST_IFNAME
ip link set $LOCAL_IFNAME up

# Step 4: Adding the virtual interface to the bridge
ip link set $GUEST_IFNAME netns $NSPID
if [ "$VLANTAG" ]
then
	ovs-vsctl add-port $BRIDGE $LOCAL_IFNAME tag=$VLANTAG -- set Interface $LOCAL_IFNAME ofport_request=$OF_PORT
else
	ovs-vsctl add-port $BRIDGE $LOCAL_IFNAME -- set Interface $LOCAL_IFNAME ofport_request=$OF_PORT
fi

# Step 5: Configure netwroking within the container
ip netns exec $NSPID ip link set $GUEST_IFNAME name eth0
ip netns exec $NSPID ip addr add $IPADDR broadcast $BROADCAST dev eth0
ip netns exec $NSPID ifconfig eth0 hw ether $MAC 
ip netns exec $NSPID ip addr add 127.0.0.1 dev lo
ip netns exec $NSPID ip link set eth0 up
ip netns exec $NSPID ip link set lo up
ip netns exec $NSPID ip route add default via $GWADDR 
