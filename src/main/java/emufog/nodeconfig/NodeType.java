package emufog.nodeconfig;

/**
 * Parent class for FogNodeType and DeviceNodeType
 * in which memoryLimit and cpuShare
 * for the Node are configured.
 */
abstract class NodeType {

    private int memoryLimit;
    private int cpuShare;

    private float nodeLatency;
    private float nodeBandwidth;

    NodeType(int memoryLimit, int cpuShare, float nodeLatency, float nodeBandwidth) {
        this.memoryLimit = memoryLimit;
        this.cpuShare = cpuShare;
        this.nodeLatency = nodeLatency;
        this.nodeBandwidth = nodeBandwidth;
    }

    public int getMemoryLimit() {
        return memoryLimit;
    }

    public int getCpuShare() {
        return cpuShare;
    }

    public void setMemoryLimit(int memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public void setCpuShare(int cpuShare) {
        this.cpuShare = cpuShare;
    }

    public float getNodeLatency() {
        return nodeLatency;
    }

    public void setNodeLatency(float nodeLatency) {
        this.nodeLatency = nodeLatency;
    }

    public float getNodeBandwidth() {
        return nodeBandwidth;
    }

    public void setNodeBandwidth(float nodeBandwidth) {
        this.nodeBandwidth = nodeBandwidth;
    }
}