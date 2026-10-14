package com.shushinestudio.servicios.interfaces;

import com.shushinestudio.dtos.timeline.TimelineItemSalida;

import java.time.LocalDate;
import java.util.List;

public interface ITimelineService {
    List<TimelineItemSalida> obtenerTimeline(LocalDate fecha, Integer estilistaId);
}
