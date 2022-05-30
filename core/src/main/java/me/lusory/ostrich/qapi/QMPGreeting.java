package me.lusory.ostrich.qapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.lusory.ostrich.qapi.control.QMPCapability;
import me.lusory.ostrich.qapi.control.VersionInfo;

import java.util.List;
import java.util.Objects;

public class QMPGreeting implements QStruct {
    @JsonProperty("QMP")
    private QMPVersion qmp;

    public QMPGreeting(QMPVersion qmp) {
        this.qmp = qmp;
    }

    public QMPGreeting() {
    }

    public QMPVersion getQmp() {
        return this.qmp;
    }

    @JsonProperty("QMP")
    public void setQmp(QMPVersion qmp) {
        this.qmp = qmp;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof QMPGreeting)) {
            return false;
        }
        final QMPGreeting other = (QMPGreeting) o;
        final Object this$qmp = this.getQmp();
        final Object other$qmp = other.getQmp();
        return Objects.equals(this$qmp, other$qmp);
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $qmp = this.getQmp();
        result = result * PRIME + ($qmp == null ? 43 : $qmp.hashCode());
        return result;
    }

    public String toString() {
        return "QMPGreeting(qmp=" + this.getQmp() + ")";
    }

    public static class QMPVersion implements QStruct {
        private VersionInfo version;
        private List<QMPCapability> capabilities;

        public QMPVersion(VersionInfo version, List<QMPCapability> capabilities) {
            this.version = version;
            this.capabilities = capabilities;
        }

        public QMPVersion() {
        }

        public VersionInfo getVersion() {
            return this.version;
        }

        public List<QMPCapability> getCapabilities() {
            return this.capabilities;
        }

        public void setVersion(VersionInfo version) {
            this.version = version;
        }

        public void setCapabilities(List<QMPCapability> capabilities) {
            this.capabilities = capabilities;
        }

        public boolean equals(final Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof QMPVersion)) {
                return false;
            }
            final QMPVersion other = (QMPVersion) o;
            final Object this$version = this.getVersion();
            final Object other$version = other.getVersion();
            if (!Objects.equals(this$version, other$version)) {
                return false;
            }
            final Object this$capabilities = this.getCapabilities();
            final Object other$capabilities = other.getCapabilities();
            return Objects.equals(this$capabilities, other$capabilities);
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $version = this.getVersion();
            result = result * PRIME + ($version == null ? 43 : $version.hashCode());
            final Object $capabilities = this.getCapabilities();
            result = result * PRIME + ($capabilities == null ? 43 : $capabilities.hashCode());
            return result;
        }

        public String toString() {
            return "QMPGreeting.QMPVersion(version=" + this.getVersion() + ", capabilities=" + this.getCapabilities() + ")";
        }
    }
}
