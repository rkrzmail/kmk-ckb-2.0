package com.kmkbe.config;

import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.util.Optional;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppConfigTest {

  @Mock
  private CustomerRepository customerRepository;

  @Mock
  private MstUserRepository internalUserRepository;

  private AppConfig appConfig;

  @BeforeEach
  void setUp() {
    appConfig = new AppConfig(customerRepository, internalUserRepository);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(customerRepository, internalUserRepository);
  }

  @Test
  void initSetsDefaultTimezoneToAsiaJakarta() {
    TimeZone previousTimeZone = TimeZone.getDefault();

    try {
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

      appConfig.init();

      assertThat(TimeZone.getDefault().getID()).isEqualTo("Asia/Jakarta");
    } finally {
      TimeZone.setDefault(previousTimeZone);
    }
  }

  @Test
  void restTemplateByPassSSLReturnsRestTemplate() {
    RestTemplate restTemplate = appConfig.restTemplateByPassSSL();

    assertThat(restTemplate).isNotNull();
  }

  @Test
  void userDetailsServiceReturnsCustomerWhenFound() {
    String username = "customer@example.com";
    Customer customer = mock(Customer.class);
    when(customerRepository.findByCustEmail(username)).thenReturn(Optional.of(customer));

    UserDetailsService userDetailsService = appConfig.userDetailsService();
    UserDetails actual = userDetailsService.loadUserByUsername(username);

    assertThat(actual).isSameAs(customer);
    verify(customerRepository).findByCustEmail(username);
  }

  @Test
  void userDetailsServiceThrowsWhenCustomerNotFound() {
    String username = "missing@example.com";
    when(customerRepository.findByCustEmail(username)).thenReturn(Optional.empty());

    UserDetailsService userDetailsService = appConfig.userDetailsService();

    assertThatThrownBy(() -> userDetailsService.loadUserByUsername(username))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessage("User not found");
    verify(customerRepository).findByCustEmail(username);
  }

  @Test
  void internalUserDetailsServiceReturnsInternalUserWhenFound() {
    String username = "internal.user";
    MstUser user = mock(MstUser.class);
    when(internalUserRepository.findByUsername(username)).thenReturn(Optional.of(user));

    UserDetailsService userDetailsService = appConfig.internalUserDetailsService();
    UserDetails actual = userDetailsService.loadUserByUsername(username);

    assertThat(actual).isSameAs(user);
    verify(internalUserRepository).findByUsername(username);
  }

  @Test
  void internalUserDetailsServiceThrowsWhenInternalUserNotFound() {
    String username = "missing.internal";
    when(internalUserRepository.findByUsername(username)).thenReturn(Optional.empty());

    UserDetailsService userDetailsService = appConfig.internalUserDetailsService();

    assertThatThrownBy(() -> userDetailsService.loadUserByUsername(username))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessage("User not found");
    verify(internalUserRepository).findByUsername(username);
  }

  @Test
  void authenticationManagerDelegatesToAuthenticationConfiguration() throws Exception {
    AuthenticationConfiguration authenticationConfiguration = mock(AuthenticationConfiguration.class);
    AuthenticationManager expected = mock(AuthenticationManager.class);
    when(authenticationConfiguration.getAuthenticationManager()).thenReturn(expected);

    AuthenticationManager actual = appConfig.authenticationManager(authenticationConfiguration);

    assertThat(actual).isSameAs(expected);
    verify(authenticationConfiguration).getAuthenticationManager();
  }

  @Test
  void authenticationProviderReturnsDaoAuthenticationProvider() {
    AuthenticationProvider authenticationProvider = appConfig.authenticationProvider();

    assertThat(authenticationProvider).isInstanceOf(DaoAuthenticationProvider.class);
  }

  @Test
  void bCryptEncoderReturnsBCryptPasswordEncoder() {
    BCryptPasswordEncoder encoder = appConfig.bCryptEncoder();

    assertThat(encoder).isNotNull();
    assertThat(encoder.matches("secret", encoder.encode("secret"))).isTrue();
  }

  @Test
  void clockReturnsSystemDefaultZoneClock() {
    Clock clock = appConfig.clock();

    assertThat(clock).isNotNull();
    assertThat(clock.getZone()).isEqualTo(TimeZone.getDefault().toZoneId());
  }
}
