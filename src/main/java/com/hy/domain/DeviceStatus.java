package com.hy.domain;

import com.google.common.base.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Dylan on 2023/4/7 15:37.
 * 21.设备上传数据：终端状态，电压、电流等
 *
 * @author Dylan
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "device_status")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DeviceStatus extends AbstractDeviceDataEntity<DeviceStatus> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 太阳能充电电流
     */
    @Column(name = "solar_charge_current")
    private Integer solarChargeCurrent;

    /**
     * 太阳能充电电压
     */
    @Column(name = "solar_charge_voltage")
    private Integer solarChargeVoltage;


    /**
     * 感应取电电流
     */
    @Column(name = "power_collect_current")
    private Integer powerCollectCurrent;

    /**
     * 感应取电电压
     */
    @Column(name = "power_collect_voltage")
    private Integer powerCollectVoltage;

    /**
     * 设备工作电流
     */
    @Column(name = "work_current")
    private Integer workCurrent;

    /**
     * 设备工作电压
     */
    @Column(name = "work_voltage")
    private Integer workVoltage;


    /**
     * 电池电压
     */
    @Column(name = "battery_voltage")
    private Integer batteryVoltage;

    /**
     * 电池电量
     */
    @Column(name = "battery_charge")
    private Float batteryCharge;

    /**
     *
     * 芯片温度
     */
    @Column(name = "chip_temperature")
    private Integer chipTemperature;

    /**
     * 主板温度
     */
    @Column(name = "mainboard_temperature")
    private Integer mainboardTemperature;

    /**
     * 信号强度
     */
    @Column(name = "signal_strength")
    private Integer signalStrength;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceStatus that = (DeviceStatus) o;
        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

}
