/*
 * Copyright 2014-2015 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.cli.net;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.onosproject.net.flow.DefaultTrafficTreatment.builder;

import java.util.LinkedList;
import java.util.List;

import org.apache.karaf.shell.commands.Option;
import org.onlab.packet.Ip6Address;
import org.onlab.packet.IpAddress;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.Link;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.Key;
import org.onosproject.net.intent.constraint.BandwidthConstraint;
import org.onosproject.net.intent.constraint.LambdaConstraint;
import org.onosproject.net.intent.constraint.LinkTypeConstraint;
import org.onosproject.net.resource.Bandwidth;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.MacAddress;

/**
 * Base class for command line operations for connectivity based intents.
 */
public abstract class ConnectivityIntentCommand extends AbstractShellCommand {

    // Selectors
    @Option(name = "-s", aliases = "--ethSrc", description = "Source MAC Address",
            required = false, multiValued = false)
    private String srcMacString = null;

    @Option(name = "-d", aliases = "--ethDst", description = "Destination MAC Address",
            required = false, multiValued = false)
    private String dstMacString = null;

    @Option(name = "-t", aliases = "--ethType", description = "Ethernet Type",
            required = false, multiValued = false)
    private String ethTypeString = null;

    @Option(name = "--ipProto", description = "IP Protocol",
            required = false, multiValued = false)
    private String ipProtoString = null;

    @Option(name = "--ipSrc", description = "Source IP Prefix",
            required = false, multiValued = false)
    private String srcIpString = null;

    @Option(name = "--ipDst", description = "Destination IP Prefix",
            required = false, multiValued = false)
    private String dstIpString = null;

    @Option(name = "--fLabel", description = "IPv6 Flow Label",
            required = false, multiValued = false)
    private String fLabelString = null;

    @Option(name = "--icmp6Type", description = "ICMPv6 Type",
            required = false, multiValued = false)
    private String icmp6TypeString = null;

    @Option(name = "--icmp6Code", description = "ICMPv6 Code",
            required = false, multiValued = false)
    private String icmp6CodeString = null;

    @Option(name = "--ndTarget", description = "IPv6 Neighbor Discovery Target Address",
            required = false, multiValued = false)
    private String ndTargetString = null;

    @Option(name = "--ndSLL", description = "IPv6 Neighbor Discovery Source Link-Layer",
            required = false, multiValued = false)
    private String ndSLLString = null;

    @Option(name = "--ndTLL", description = "IPv6 Neighbor Discovery Target Link-Layer",
            required = false, multiValued = false)
    private String ndTLLString = null;

    @Option(name = "--tcpSrc", description = "Source TCP Port",
            required = false, multiValued = false)
    private String srcTcpString = null;

    @Option(name = "--tcpDst", description = "Destination TCP Port",
            required = false, multiValued = false)
    private String dstTcpString = null;

    @Option(name = "--extHdr", description = "IPv6 Extension Header Pseudo-field",
            required = false, multiValued = false)
    private String extHdrString = null;

    @Option(name = "-b", aliases = "--bandwidth", description = "Bandwidth",
            required = false, multiValued = false)
    private String bandwidthString = null;

    @Option(name = "-l", aliases = "--lambda", description = "Lambda",
            required = false, multiValued = false)
    private boolean lambda = false;

    @Option(name = "-a", aliases = "--appId", description = "Application Id",
            required = false, multiValued = false)
    private String appId = null;

    @Option(name = "-k", aliases = "--key", description = "Intent Key",
            required = false, multiValued = false)
    private String intentKey = null;


    // Treatments
    @Option(name = "--setEthSrc", description = "Rewrite Source MAC Address",
            required = false, multiValued = false)
    private String setEthSrcString = null;

    @Option(name = "--setEthDst", description = "Rewrite Destination MAC Address",
            required = false, multiValued = false)
    private String setEthDstString = null;

    @Option(name = "--setIpSrc", description = "Rewrite Source IP Address",
            required = false, multiValued = false)
    private String setIpSrcString = null;

    @Option(name = "--setIpDst", description = "Rewrite Destination IP Address",
            required = false, multiValued = false)
    private String setIpDstString = null;


    // Priorities
    @Option(name = "-p", aliases = "--priority", description = "Priority",
            required = false, multiValued = false)
    private int priority = Intent.DEFAULT_INTENT_PRIORITY;

    /**
     * Constructs a traffic selector based on the command line arguments
     * presented to the command.
     * @return traffic selector
     */
    protected TrafficSelector buildTrafficSelector() {
        IpPrefix srcIpPrefix = null;
        IpPrefix dstIpPrefix = null;

        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();

        if (!isNullOrEmpty(srcIpString)) {
            srcIpPrefix = IpPrefix.valueOf(srcIpString);
            if (srcIpPrefix.isIp4()) {
                selectorBuilder.matchIPSrc(srcIpPrefix);
            } else {
                selectorBuilder.matchIPv6Src(srcIpPrefix);
            }
        }

        if (!isNullOrEmpty(dstIpString)) {
            dstIpPrefix = IpPrefix.valueOf(dstIpString);
            if (dstIpPrefix.isIp4()) {
                selectorBuilder.matchIPDst(dstIpPrefix);
            } else {
                selectorBuilder.matchIPv6Dst(dstIpPrefix);
            }
        }

        if ((srcIpPrefix != null) && (dstIpPrefix != null) &&
            (srcIpPrefix.version() != dstIpPrefix.version())) {
            // ERROR: IP src/dst version mismatch
            throw new IllegalArgumentException(
                        "IP source and destination version mismatch");
        }

        //
        // Set the default EthType based on the IP version if the matching
        // source or destination IP prefixes.
        //
        short ethType = EthType.IPV4.value();
        if ((srcIpPrefix != null) && srcIpPrefix.isIp6()) {
            ethType = EthType.IPV6.value();
        }
        if ((dstIpPrefix != null) && dstIpPrefix.isIp6()) {
            ethType = EthType.IPV6.value();
        }
        if (!isNullOrEmpty(ethTypeString)) {
            ethType = EthType.parseFromString(ethTypeString);
        }
        selectorBuilder.matchEthType(ethType);

        if (!isNullOrEmpty(srcMacString)) {
            selectorBuilder.matchEthSrc(MacAddress.valueOf(srcMacString));
        }

        if (!isNullOrEmpty(dstMacString)) {
            selectorBuilder.matchEthDst(MacAddress.valueOf(dstMacString));
        }

        if (!isNullOrEmpty(ipProtoString)) {
            short ipProtoShort = IpProtocol.parseFromString(ipProtoString);
            selectorBuilder.matchIPProtocol((byte) ipProtoShort);
        }

        if (!isNullOrEmpty(fLabelString)) {
            selectorBuilder.matchIPv6FlowLabel(Integer.parseInt(fLabelString));
        }

        if (!isNullOrEmpty(icmp6TypeString)) {
            selectorBuilder.matchIcmpv6Type((byte) Integer.parseInt(icmp6TypeString));
        }

        if (!isNullOrEmpty(icmp6CodeString)) {
            selectorBuilder.matchIcmpv6Code((byte) Integer.parseInt(icmp6CodeString));
        }

        if (!isNullOrEmpty(ndTargetString)) {
            selectorBuilder.matchIPv6NDTargetAddress(Ip6Address.valueOf(ndTargetString));
        }

        if (!isNullOrEmpty(ndSLLString)) {
            selectorBuilder.matchIPv6NDSourceLinkLayerAddress(MacAddress.valueOf(ndSLLString));
        }

        if (!isNullOrEmpty(ndTLLString)) {
            selectorBuilder.matchIPv6NDTargetLinkLayerAddress(MacAddress.valueOf(ndTLLString));
        }

        if (!isNullOrEmpty(srcTcpString)) {
            selectorBuilder.matchTcpSrc((short) Integer.parseInt(srcTcpString));
        }

        if (!isNullOrEmpty(dstTcpString)) {
            selectorBuilder.matchTcpDst((short) Integer.parseInt(dstTcpString));
        }

        if (!isNullOrEmpty(extHdrString)) {
            selectorBuilder.matchIPv6ExthdrFlags(Integer.parseInt(extHdrString));
        }

        return selectorBuilder.build();
    }

    /**
     * Generates a traffic treatment for this intent based on command line
     * arguments presented to the command.
     *
     * @return traffic treatment
     */
    protected TrafficTreatment buildTrafficTreatment() {
        final TrafficTreatment.Builder treatmentBuilder = builder();

        if (!isNullOrEmpty(setEthSrcString)) {
            treatmentBuilder.setEthSrc(MacAddress.valueOf(setEthSrcString));
        }

        if (!isNullOrEmpty(setEthDstString)) {
            treatmentBuilder.setEthDst(MacAddress.valueOf(setEthDstString));
        }

        if (!isNullOrEmpty(setIpSrcString)) {
            treatmentBuilder.setIpSrc(IpAddress.valueOf(setIpSrcString));
        }

        if (!isNullOrEmpty(setIpDstString)) {
            treatmentBuilder.setIpSrc(IpAddress.valueOf(setIpDstString));
        }

        return treatmentBuilder.build();
    }

    /**
     * Builds the constraint list for this command based on the command line
     * parameters.
     *
     * @return List of constraint objects describing the constraints requested
     */
    protected List<Constraint> buildConstraints() {
        final List<Constraint> constraints = new LinkedList<>();

        // Check for a bandwidth specification
        if (!isNullOrEmpty(bandwidthString)) {
            final double bandwidthValue = Double.parseDouble(bandwidthString);
            constraints.add(new BandwidthConstraint(Bandwidth.bps(bandwidthValue)));
        }

        // Check for a lambda specification
        if (lambda) {
            constraints.add(new LambdaConstraint(null));
        }
        constraints.add(new LinkTypeConstraint(lambda, Link.Type.OPTICAL));

        return constraints;
    }

    @Override
    protected ApplicationId appId() {
        ApplicationId appIdForIntent;
        if (appId == null) {
            appIdForIntent = super.appId();
        } else {
            CoreService service = get(CoreService.class);
            appIdForIntent = service.getAppId(appId);
        }
        return appIdForIntent;
    }

    /**
     * Creates a key for an intent based on command line arguments.  If a key
     * has been specified, it is returned.  If no key is specified, null
     * is returned.
     *
     * @return intent key if specified, null otherwise
     */
    protected Key key() {
        Key key = null;
        ApplicationId appIdForIntent;

        if (intentKey != null) {
            key = Key.of(intentKey, appId());
        }
        return key;
    }

    /**
     * Gets the priority to use for the intent.
     *
     * @return priority
     */
    protected int priority() {
        return priority;
    }
}
