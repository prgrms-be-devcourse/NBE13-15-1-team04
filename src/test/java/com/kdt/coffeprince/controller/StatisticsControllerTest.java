package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.dto.MenuStatistics;
import com.kdt.coffeprince.dto.OrderStatistics;
import com.kdt.coffeprince.service.StatisticsService;
import com.kdt.coffeprince.util.DateUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {

    private static final String URL = "/api/v1/statistics/orders";
    private static final String MENU_URL = "/api/v1/statistics/menus";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatisticsService statisticsService;

    @Test
    void getOrderStatistics_with_multiple_orders_returns_200() throws Exception {
        LocalDate date = LocalDate.of(2026, 11, 1);
        given(this.statisticsService.getOrderStatisticsByDate(any(LocalDate.class)))
                .willReturn(new OrderStatistics(
                        date,
                        3,
                        2,
                        1,
                        12,
                        500_200));

        this.mockMvc.perform(get(URL)
                        .param("date", date.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.date").value(date.toString()))
                .andExpect(jsonPath("$.orderCount").value(3))
                .andExpect(jsonPath("$.placedOrderCount").value(2))
                .andExpect(jsonPath("$.cancelledOrderCount").value(1))
                .andExpect(jsonPath("$.totalQuantity").value(12))
                .andExpect(jsonPath("$.totalAmount").value(500_200));


        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(this.statisticsService).getOrderStatisticsByDate(captor.capture());
        assertEquals(date, captor.getValue());
    }


    @Test
    void getOrderStatistics_with_no_orders_returns_200_with_zero_counts() throws Exception {
        LocalDate date = LocalDate.of(2026, 11, 1);
        given(this.statisticsService.getOrderStatisticsByDate(any(LocalDate.class)))
                .willReturn(new OrderStatistics(
                        date,
                        0,
                        0,
                        0,
                        0,
                        0));

        this.mockMvc.perform(get(URL)
                        .param("date", date.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.date").value(date.toString()))
                .andExpect(jsonPath("$.orderCount").value(0))
                .andExpect(jsonPath("$.placedOrderCount").value(0))
                .andExpect(jsonPath("$.cancelledOrderCount").value(0))
                .andExpect(jsonPath("$.totalQuantity").value(0))
                .andExpect(jsonPath("$.totalAmount").value(0));

        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(this.statisticsService).getOrderStatisticsByDate(captor.capture());
        assertEquals(date, captor.getValue());
    }


    @Test
    void getOrderStatistics_without_date_uses_today() throws Exception {
        LocalDate today = DateUtils.today();
        given(this.statisticsService.getOrderStatisticsByDate(any(LocalDate.class)))
                .willReturn(new OrderStatistics(today, 0, 0, 0, 0, 0));

        this.mockMvc.perform(get(URL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.date").value(today.toString()));

        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(this.statisticsService).getOrderStatisticsByDate(captor.capture());
        assertEquals(today, captor.getValue());
    }

    @Test
    void future_date_is_400_before_service() throws Exception {
        LocalDate tomorrow = DateUtils.today().plusDays(1);

        this.mockMvc.perform(get(URL)
                        .param("date", tomorrow.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("미래 날짜는 조회할 수 없습니다"));

        verifyNoInteractions(this.statisticsService);
    }

    @Test
    void getMenuStatistics_with_multiple_menus_returns_200() throws Exception {
        LocalDate date = LocalDate.of(2026, 11, 1);
        given(this.statisticsService.getMenuStatisticsByDate(any(LocalDate.class)))
                .willReturn(List.of(
                        new MenuStatistics(1L, "아메리카노", 4_500, 10, 45_000),
                        new MenuStatistics(2L, "카페라떼", 5_000, 4, 20_000)));

        this.mockMvc.perform(get(MENU_URL)
                        .param("date", date.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].menuId").value(1))
                .andExpect(jsonPath("$[0].menuName").value("아메리카노"))
                .andExpect(jsonPath("$[0].unitPrice").value(4_500))
                .andExpect(jsonPath("$[0].totalQuantity").value(10))
                .andExpect(jsonPath("$[0].totalAmount").value(45_000))
                .andExpect(jsonPath("$[1].menuId").value(2))
                .andExpect(jsonPath("$[1].menuName").value("카페라떼"))
                .andExpect(jsonPath("$[1].unitPrice").value(5_000))
                .andExpect(jsonPath("$[1].totalQuantity").value(4))
                .andExpect(jsonPath("$[1].totalAmount").value(20_000));

        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(this.statisticsService).getMenuStatisticsByDate(captor.capture());
        assertEquals(date, captor.getValue());
    }

    @Test
    void getMenuStatistics_with_no_orders_returns_200_with_empty_list() throws Exception {
        LocalDate date = LocalDate.of(2026, 11, 1);
        given(this.statisticsService.getMenuStatisticsByDate(any(LocalDate.class)))
                .willReturn(List.of());

        this.mockMvc.perform(get(MENU_URL)
                        .param("date", date.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(this.statisticsService).getMenuStatisticsByDate(captor.capture());
        assertEquals(date, captor.getValue());
    }

    @Test
    void getMenuStatistics_without_date_uses_today() throws Exception {
        LocalDate today = DateUtils.today();
        given(this.statisticsService.getMenuStatisticsByDate(any(LocalDate.class)))
                .willReturn(List.of());

        this.mockMvc.perform(get(MENU_URL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(this.statisticsService).getMenuStatisticsByDate(captor.capture());
        assertEquals(today, captor.getValue());
    }
    
    @Test
    void getMenuStatistics_with_future_date_returns_400_before_service() throws Exception {
        LocalDate tomorrow = DateUtils.today().plusDays(1);

        this.mockMvc.perform(get(MENU_URL)
                        .param("date", tomorrow.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("미래 날짜는 조회할 수 없습니다"));

        verifyNoInteractions(this.statisticsService);
    }
}
