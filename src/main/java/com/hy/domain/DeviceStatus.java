package com.hy.domain;

import com.google.common.base.Objects;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Dylan on 2023/4/7 15:37.
 * 21.设备上传数据：终端状态，电压、电流等
 *
 * @author Dylan
 */
@Getter
@Setter
@ToString
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
     * 相线取电电流
     */
    @Column(name = "phase_power_current")
    private Integer phasePowerCurrent;

    /**
     * 设备工作电压
     */
    @Column(name = "work_voltage")
    private Integer workVoltage;

    /**
     * 设备工作电流
     */
    @Column(name = "work_current")
    private Integer workCurrent;


    /**
     * 电池电压
     */
    @Column(name = "battery_voltage")
    private Integer batteryVoltage;

    /**
     * 保留位
     */
    @Column(name = "reserved")
    private Integer reserved;

    /**
     * 太阳能板A路电压
     */
    private Integer solarPanelAVoltage;

    /**
     * 太阳能板A路电压
     */
    private Integer solarPanelBVoltage;

    /**
     * 太阳能板A路电压
     */
    private Integer solarPanelCVoltage;

    /**
     * 相线取电电压
     */
    private Integer phasePowerVoltage;

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

    /**
     * GPS纬度
     */
    private Integer gpsLatitude;

    /**
     * GPS经度
     */
    private Integer gpsLongitude;


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
