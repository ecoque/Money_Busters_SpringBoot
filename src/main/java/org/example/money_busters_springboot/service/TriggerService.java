package org.example.money_busters_springboot.service;

import org.example.money_busters_springboot.model.TriggerMetadata;
import org.example.money_busters_springboot.repository.TriggerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Trigger işlemleri için service sınıfı
 */
@Service
public class TriggerService {

    private final TriggerRepository triggerRepository;

    public TriggerService(TriggerRepository triggerRepository) {
        this.triggerRepository = triggerRepository;
    }

    /**
     * Tüm trigger'ları listeler
     */
    public List<TriggerMetadata> getAllTriggers() {
        return triggerRepository.findAllTriggers();
    }

    /**
     * Belirli bir tabloya ait trigger'ları listeler
     */
    public List<TriggerMetadata> getTriggersByTable(String tableName) {
        return triggerRepository.findTriggersByTableName(tableName);
    }

    /**
     * Trigger adına göre trigger bilgisini getirir
     */
    public TriggerMetadata getTriggerByName(String triggerName) {
        return triggerRepository.findTriggerByName(triggerName);
    }

    /**
     * Trigger'ı aktif yapar
     */
    public void enableTrigger(String triggerName) {
        triggerRepository.setTriggerStatus(triggerName, true);
    }

    /**
     * Trigger'ı pasif yapar
     */
    public void disableTrigger(String triggerName) {
        triggerRepository.setTriggerStatus(triggerName, false);
    }
}
