package org.apache.mesos.kafka.config;

import java.util.List;
import java.util.Map;

import org.apache.mesos.config.ConfigProperty;
import org.apache.mesos.config.FrameworkConfigurationService;

import com.google.common.collect.Lists;

/**
 * Read-only retrieval service for a given Kafka framework configuration.
 * All access is via helper functions which retrieve the requested values from the underlying data.
 */
public class KafkaConfigService extends FrameworkConfigurationService {

  /**
   * Simple structure for returning the values of per-broker resources to be reserved.
   */
  public static class BrokerResources {
    private BrokerResources(String cpus, String mem, String disk) {
      this.cpus = Double.parseDouble(cpus);
      this.mem = Double.parseDouble(mem);
      this.disk = Double.parseDouble(disk);
    }

    public final double cpus;
    public final double mem;
    public final double disk;
  }

  private static KafkaConfigService envConfig = null;

  public static KafkaConfigService getEnvConfig() {
    if (null == envConfig) {
      envConfig = new KafkaConfigService();
      KafkaEnvConfigurator envConfigurator = new KafkaEnvConfigurator();
      envConfigurator.configure(envConfig);
    }

    return envConfig;
  }

  public static KafkaConfigService getHydratedConfig(
      Map<String, Map<String, ConfigProperty>> nsMap) {

    KafkaConfigService configService = new KafkaConfigService();
    ZkHydratorConfigurator zkConfigurator = new ZkHydratorConfigurator(nsMap);
    zkConfigurator.configure(configService);

    return configService;
  }

  public String getZkRoot() {
    return "/" + get("FRAMEWORK_NAME");
  }

  public String getKafkaZkUri() {
    return getZookeeperAddress() + getZkRoot();
  }

  public String getZookeeperAddress() {
    return "master.mesos:2181";
  }

  public int getBrokerCount() {
    return Integer.parseInt(get("BROKER_COUNT"));
  }

  /**
   * Returns the desired resources to allocate for each Kafka broker.
   */
  public BrokerResources getBrokerResources() {
    return new BrokerResources(
        get("BROKER_CPUS"), get("BROKER_MEM"), get("BROKER_DISK"));
  }

  /**
   * Returns the list of mesos resource URLs to be downloaded/unpacked before starting Kafka brokers.
   */
  public List<String> getBrokerResourceUris() {
    return Lists.newArrayList(
        get("KAFKA_URI"),
        get("CONTAINER_HOOK_URI"),
        get("JAVA_URI"),
        get("OVERRIDER_URI"));
  }

  public String getFrameworkName() {
    return get("FRAMEWORK_NAME");
  }

  public String getRole() {
    return getFrameworkName() + "-role";
  }

  public String getPrincipal() {
    return getFrameworkName() + "-principal";
  }

  public String getKafkaVersionName() {
    return get("KAFKA_VER_NAME");
  }

  /**
   * Returns the name of the configured Plan strategy, eg "INSTALL".
   */
  public String getPlanStrategy() {
    return get("PLAN_STRATEGY");
  }

  /**
   * Returns the name of the configured placement strategy, eg "NODE".
   */
  public String getPlacementStrategy() {
    return get("PLACEMENT_STRATEGY");
  }

  public boolean advertisedHost() {
    String overrideStr = get("KAFKA_ADVERTISE_HOST_IP");
    return Boolean.parseBoolean(overrideStr);
  }

  public String getOverridePrefix() {
    return "KAFKA_OVERRIDE_";
  }
}
