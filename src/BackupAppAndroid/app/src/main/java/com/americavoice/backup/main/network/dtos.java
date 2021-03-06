/* Options:
Date: 2018-01-07 20:31:01
Version: 4.512
Tip: To override a DTO option, remove "//" prefix before updating
BaseUrl: http://backupapi.secureip.io/api

Package: com.americavoice.backup.main.network
GlobalNamespace: dtos
//AddPropertyAccessors: True
//SettersReturnThis: True
//AddServiceStackTypes: True
//AddResponseStatus: False
//AddDescriptionAsComments: True
//AddImplicitVersion: 
//IncludeTypes: 
//ExcludeTypes: 
//TreatTypesAsStrings: 
//DefaultImports: java.math.*,java.util.*,net.servicestack.client.*,com.google.gson.annotations.*,com.google.gson.reflect.*
*/

package com.americavoice.backup.main.network;

import java.math.*;
import java.util.*;
import net.servicestack.client.*;
import com.google.gson.annotations.*;
import com.google.gson.reflect.*;

public class dtos
{

    /**
    * Query login history
    */
    @Route(Path="/login-history", Verbs="GET")
    @Api(Description="Query login history")
    public static class FindLoginHistory extends QueryDb<UserLoginHistory> implements IReturn<QueryResponse<UserLoginHistory>>
    {
        public String accountNumber = null;
        
        public String getAccountNumber() { return accountNumber; }
        public FindLoginHistory setAccountNumber(String value) { this.accountNumber = value; return this; }
        private static Object responseType = new TypeToken<QueryResponse<UserLoginHistory>>(){}.getType();
        public Object getResponseType() { return responseType; }
    }

    /**
    * Query login metadata
    */
    @Route(Path="/login-metadata", Verbs="GET")
    @Api(Description="Query login metadata")
    public static class FindLoginMeta extends QueryDb<UserLoginMeta> implements IReturn<QueryResponse<UserLoginMeta>>
    {
        public String accountNumber = null;
        
        public String getAccountNumber() { return accountNumber; }
        public FindLoginMeta setAccountNumber(String value) { this.accountNumber = value; return this; }
        private static Object responseType = new TypeToken<QueryResponse<UserLoginMeta>>(){}.getType();
        public Object getResponseType() { return responseType; }
    }

    /**
    * Time zone information.
    */
    @Route(Path="/timezones", Verbs="GET")
    @Api(Description="Time zone information.")
    public static class GetTimeZones implements IReturn<GetTimeZonesResponse>
    {
        
        private static Object responseType = GetTimeZonesResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Get config for the mobile app.
    */
    @Route(Path="/mobileapp/config", Verbs="GET")
    @Api(Description="Get config for the mobile app.")
    public static class GetMobileAppConfig implements IReturn<GetMobileAppConfigResponse>
    {
        /**
        * Tablet (true, false).
        */
        @ApiMember(Description="Tablet (true, false).", IsRequired=true, ParameterType="form")
        public Boolean tablet = null;

        /**
        * Type (android, ios).
        */
        @ApiMember(Description="Type (android, ios).", IsRequired=true, ParameterType="form")
        public String type = null;
        
        public Boolean isTablet() { return tablet; }
        public GetMobileAppConfig setTablet(Boolean value) { this.tablet = value; return this; }
        public String getType() { return type; }
        public GetMobileAppConfig setType(String value) { this.type = value; return this; }
        private static Object responseType = GetMobileAppConfigResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Get countries where the service is available.
    */
    @Route(Path="/countries", Verbs="GET")
    @Api(Description="Get countries where the service is available.")
    public static class GetCountries implements IReturn<GetCountriesResponse>
    {
        /**
        * Language of country names that will be returned
        */
        @ApiMember(Description="Language of country names that will be returned", IsRequired=true, ParameterType="query")
        public String language = null;
        
        public String getLanguage() { return language; }
        public GetCountries setLanguage(String value) { this.language = value; return this; }
        private static Object responseType = GetCountriesResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Register new customer.
    */
    @Route(Path="/register", Verbs="POST")
    @Api(Description="Register new customer.")
    public static class CustomRegister implements IReturn<CustomRegisterResponse>
    {
        /**
        * Company Id.
        */
        @ApiMember(Description="Company Id.", IsRequired=true, ParameterType="form")
        public Integer companyId = null;

        /**
        * Username
        */
        @ApiMember(Description="Username", IsRequired=true, ParameterType="form")
        public String username = null;

        /**
        * Country Calling Code 
        */
        @ApiMember(Description="Country Calling Code ", IsRequired=true, ParameterType="form")
        public String countryCallingCode = null;

        /**
        * Valid phone number.
        */
        @ApiMember(Description="Valid phone number.", IsRequired=true, ParameterType="form")
        public String phoneNumber = null;

        /**
        * Password 
        */
        @ApiMember(Description="Password ", IsRequired=true, ParameterType="form")
        public String password = null;

        /**
        * Application 
        */
        @ApiMember(Description="Application ", IsRequired=true, ParameterType="form")
        public String application = null;
        
        public Integer getCompanyId() { return companyId; }
        public CustomRegister setCompanyId(Integer value) { this.companyId = value; return this; }
        public String getUsername() { return username; }
        public CustomRegister setUsername(String value) { this.username = value; return this; }
        public String getCountryCallingCode() { return countryCallingCode; }
        public CustomRegister setCountryCallingCode(String value) { this.countryCallingCode = value; return this; }
        public String getPhoneNumber() { return phoneNumber; }
        public CustomRegister setPhoneNumber(String value) { this.phoneNumber = value; return this; }
        public String getPassword() { return password; }
        public CustomRegister setPassword(String value) { this.password = value; return this; }
        public String getApplication() { return application; }
        public CustomRegister setApplication(String value) { this.application = value; return this; }
        private static Object responseType = CustomRegisterResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Send reset password sms.
    */
    @Route(Path="/user/send-reset-password-sms", Verbs="POST")
    @Api(Description="Send reset password sms.")
    public static class SendPasswordResetCode implements IReturn<SendPasswordResetCodeResponse>
    {
        /**
        * Company Id.
        */
        @ApiMember(Description="Company Id.", IsRequired=true, ParameterType="form")
        public Integer companyId = null;

        /**
        * Country Calling Code.
        */
        @ApiMember(Description="Country Calling Code.", IsRequired=true, ParameterType="form")
        public String countryCallingCode = null;

        /**
        * Phone number.
        */
        @ApiMember(Description="Phone number.", IsRequired=true, ParameterType="form")
        public String phoneNumber = null;

        /**
        * Application 
        */
        @ApiMember(Description="Application ", IsRequired=true, ParameterType="form")
        public String application = null;
        
        public Integer getCompanyId() { return companyId; }
        public SendPasswordResetCode setCompanyId(Integer value) { this.companyId = value; return this; }
        public String getCountryCallingCode() { return countryCallingCode; }
        public SendPasswordResetCode setCountryCallingCode(String value) { this.countryCallingCode = value; return this; }
        public String getPhoneNumber() { return phoneNumber; }
        public SendPasswordResetCode setPhoneNumber(String value) { this.phoneNumber = value; return this; }
        public String getApplication() { return application; }
        public SendPasswordResetCode setApplication(String value) { this.application = value; return this; }
        private static Object responseType = SendPasswordResetCodeResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Reset password.
    */
    @Route(Path="/user/reset-password", Verbs="POST")
    @Api(Description="Reset password.")
    public static class PerformResetPassword implements IReturn<PerformResetPasswordResponse>
    {
        /**
        * Company ID
        */
        @ApiMember(Description="Company ID", IsRequired=true, ParameterType="form")
        public Integer companyId = null;

        /**
        * Country Calling Code
        */
        @ApiMember(Description="Country Calling Code", IsRequired=true, ParameterType="form")
        public String countryCallingCode = null;

        /**
        * Phone Number.
        */
        @ApiMember(Description="Phone Number.", IsRequired=true, ParameterType="form")
        public String phoneNumber = null;

        /**
        * Reset password code.
        */
        @ApiMember(Description="Reset password code.", IsRequired=true, ParameterType="form")
        public String resetPasswordCode = null;

        /**
        * New password.
        */
        @ApiMember(Description="New password.", IsRequired=true, ParameterType="form")
        public String newPassword = null;

        /**
        * Application 
        */
        @ApiMember(Description="Application ", IsRequired=true, ParameterType="form")
        public String application = null;
        
        public Integer getCompanyId() { return companyId; }
        public PerformResetPassword setCompanyId(Integer value) { this.companyId = value; return this; }
        public String getCountryCallingCode() { return countryCallingCode; }
        public PerformResetPassword setCountryCallingCode(String value) { this.countryCallingCode = value; return this; }
        public String getPhoneNumber() { return phoneNumber; }
        public PerformResetPassword setPhoneNumber(String value) { this.phoneNumber = value; return this; }
        public String getResetPasswordCode() { return resetPasswordCode; }
        public PerformResetPassword setResetPasswordCode(String value) { this.resetPasswordCode = value; return this; }
        public String getNewPassword() { return newPassword; }
        public PerformResetPassword setNewPassword(String value) { this.newPassword = value; return this; }
        public String getApplication() { return application; }
        public PerformResetPassword setApplication(String value) { this.application = value; return this; }
        private static Object responseType = PerformResetPasswordResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Send phone verification code to authenticated user.
    */
    @Route(Path="/register/send-phonecode", Verbs="POST")
    @Api(Description="Send phone verification code to authenticated user.")
    public static class SendPhoneVerificationCode implements IReturn<SendPhoneVerificationCodeResponse>
    {
        /**
        * Application 
        */
        @ApiMember(Description="Application ", IsRequired=true, ParameterType="form")
        public String application = null;
        
        public String getApplication() { return application; }
        public SendPhoneVerificationCode setApplication(String value) { this.application = value; return this; }
        private static Object responseType = SendPhoneVerificationCodeResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Validate phone verification code for authenticated user.
    */
    @Route(Path="/register/validate-phonecode", Verbs="POST")
    @Api(Description="Validate phone verification code for authenticated user.")
    public static class ValidatePhoneVerificationCode implements IReturn<ValidatePhoneVerificationCodeResponse>
    {
        /**
        * Verification Code.
        */
        @ApiMember(Description="Verification Code.", IsRequired=true, ParameterType="form")
        public String verificationCode = null;

        /**
        * Application 
        */
        @ApiMember(Description="Application ", IsRequired=true, ParameterType="form")
        public String application = null;
        
        public String getVerificationCode() { return verificationCode; }
        public ValidatePhoneVerificationCode setVerificationCode(String value) { this.verificationCode = value; return this; }
        public String getApplication() { return application; }
        public ValidatePhoneVerificationCode setApplication(String value) { this.application = value; return this; }
        private static Object responseType = ValidatePhoneVerificationCodeResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Get authenticated user.
    */
    @Route(Path="/user", Verbs="GET")
    @Api(Description="Get authenticated user.")
    public static class GetFullUser implements IReturn<GetFullUserResponse>
    {
        
        private static Object responseType = GetFullUserResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Get Current User session.
    */
    @Route(Path="/user/session", Verbs="GET")
    @Api(Description="Get Current User session.")
    public static class GetSession implements IReturn<GetSessionResponse>
    {
        
        private static Object responseType = GetSessionResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Get Account.
    */
    @Route(Path="/authorization/users/account", Verbs="GET")
    @Api(Description="Get Account.")
    public static class GetUserProfile implements IReturn<GetUserProfileResponse>
    {
        
        private static Object responseType = GetUserProfileResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Update Account
    */
    @Route(Path="/authorization/users/", Verbs="PUT")
    @Api(Description="Update Account")
    public static class UpdateUserProfile implements IReturn<UpdateUserProfileReponse>
    {
        public UserModel user = null;
        public String newPassword = null;
        
        public UserModel getUser() { return user; }
        public UpdateUserProfile setUser(UserModel value) { this.user = value; return this; }
        public String getNewPassword() { return newPassword; }
        public UpdateUserProfile setNewPassword(String value) { this.newPassword = value; return this; }
        private static Object responseType = UpdateUserProfileReponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Change password.
    */
    @Route(Path="/user/change-password", Verbs="POST")
    @Api(Description="Change password.")
    public static class PerformChangePassword implements IReturn<PerformResetPasswordResponse>
    {
        /**
        * Username.
        */
        @ApiMember(Description="Username.", IsRequired=true, ParameterType="form")
        public String username = null;

        /**
        * New password.
        */
        @ApiMember(Description="New password.", IsRequired=true, ParameterType="form")
        public String newPassword = null;
        
        public String getUsername() { return username; }
        public PerformChangePassword setUsername(String value) { this.username = value; return this; }
        public String getNewPassword() { return newPassword; }
        public PerformChangePassword setNewPassword(String value) { this.newPassword = value; return this; }
        private static Object responseType = PerformResetPasswordResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Get the user list.
    */
    @Route(Path="/authorization/users", Verbs="GET")
    @Api(Description="Get the user list.")
    public static class GetUsers implements IReturn<GetUsersResponse>
    {
        @ApiMember(ParameterType="query")
        public String filter = null;
        
        public String getFilter() { return filter; }
        public GetUsers setFilter(String value) { this.filter = value; return this; }
        private static Object responseType = GetUsersResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Get user by Id.
    */
    @Route(Path="/authorization/users/{Id}", Verbs="GET")
    @Api(Description="Get user by Id.")
    public static class GetUser implements IReturn<GetUserResponse>
    {
        public Integer id = null;
        
        public Integer getId() { return id; }
        public GetUser setId(Integer value) { this.id = value; return this; }
        private static Object responseType = GetUserResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Create a new user.
    */
    @Route(Path="/authorization/users", Verbs="POST")
    @Api(Description="Create a new user.")
    public static class CreateUser implements IReturn<CreateUserResponse>
    {
        @ApiMember(IsRequired=true, ParameterType="body")
        public Integer companyId = null;

        @ApiMember(IsRequired=true, ParameterType="body")
        public String email = null;

        @ApiMember(IsRequired=true, ParameterType="body")
        public String phoneNumber = null;

        @ApiMember(IsRequired=true, ParameterType="body")
        public String userName = null;

        @ApiMember(IsRequired=true, ParameterType="body")
        public String firstName = null;

        @ApiMember(IsRequired=true, ParameterType="body")
        public String lastName = null;

        @ApiMember(IsRequired=true, ParameterType="body")
        public String timeZone = null;

        @ApiMember(IsRequired=true, ParameterType="body")
        public String language = null;

        @ApiMember(IsRequired=true, ParameterType="body")
        public String password = null;

        @ApiMember(ParameterType="body")
        public String role = null;
        
        public Integer getCompanyId() { return companyId; }
        public CreateUser setCompanyId(Integer value) { this.companyId = value; return this; }
        public String getEmail() { return email; }
        public CreateUser setEmail(String value) { this.email = value; return this; }
        public String getPhoneNumber() { return phoneNumber; }
        public CreateUser setPhoneNumber(String value) { this.phoneNumber = value; return this; }
        public String getUserName() { return userName; }
        public CreateUser setUserName(String value) { this.userName = value; return this; }
        public String getFirstName() { return firstName; }
        public CreateUser setFirstName(String value) { this.firstName = value; return this; }
        public String getLastName() { return lastName; }
        public CreateUser setLastName(String value) { this.lastName = value; return this; }
        public String getTimeZone() { return timeZone; }
        public CreateUser setTimeZone(String value) { this.timeZone = value; return this; }
        public String getLanguage() { return language; }
        public CreateUser setLanguage(String value) { this.language = value; return this; }
        public String getPassword() { return password; }
        public CreateUser setPassword(String value) { this.password = value; return this; }
        public String getRole() { return role; }
        public CreateUser setRole(String value) { this.role = value; return this; }
        private static Object responseType = CreateUserResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Update user
    */
    @Route(Path="/authorization/users/{UserId}", Verbs="PUT")
    @Api(Description="Update user")
    public static class UpdateUser implements IReturn<UpdateUserResponse>
    {
        public Integer userId = null;
        @ApiMember(IsRequired=true, ParameterType="body")
        public String deviceId = null;

        @ApiMember(IsRequired=true, ParameterType="body")
        public Integer regionId = null;

        public String email = null;
        @ApiMember(IsRequired=true, ParameterType="body")
        public String userName = null;

        @ApiMember(IsRequired=true, ParameterType="body")
        public String firstName = null;

        @ApiMember(IsRequired=true, ParameterType="body")
        public String lastName = null;

        @ApiMember(IsRequired=true, ParameterType="body")
        public String timeZone = null;

        @ApiMember(IsRequired=true, ParameterType="body")
        public String language = null;

        @ApiMember(IsRequired=true, ParameterType="body")
        public String newPassword = null;
        
        public Integer getUserId() { return userId; }
        public UpdateUser setUserId(Integer value) { this.userId = value; return this; }
        public String getDeviceId() { return deviceId; }
        public UpdateUser setDeviceId(String value) { this.deviceId = value; return this; }
        public Integer getRegionId() { return regionId; }
        public UpdateUser setRegionId(Integer value) { this.regionId = value; return this; }
        public String getEmail() { return email; }
        public UpdateUser setEmail(String value) { this.email = value; return this; }
        public String getUserName() { return userName; }
        public UpdateUser setUserName(String value) { this.userName = value; return this; }
        public String getFirstName() { return firstName; }
        public UpdateUser setFirstName(String value) { this.firstName = value; return this; }
        public String getLastName() { return lastName; }
        public UpdateUser setLastName(String value) { this.lastName = value; return this; }
        public String getTimeZone() { return timeZone; }
        public UpdateUser setTimeZone(String value) { this.timeZone = value; return this; }
        public String getLanguage() { return language; }
        public UpdateUser setLanguage(String value) { this.language = value; return this; }
        public String getNewPassword() { return newPassword; }
        public UpdateUser setNewPassword(String value) { this.newPassword = value; return this; }
        private static Object responseType = UpdateUserResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Delete user.
    */
    @Route(Path="/authorization/roles/{Id}", Verbs="DELETE")
    @Api(Description="Delete user.")
    public static class DeleteUser implements IReturn<DeleteUserResponse>
    {
        public Integer id = null;
        
        public Integer getId() { return id; }
        public DeleteUser setId(Integer value) { this.id = value; return this; }
        private static Object responseType = DeleteUserResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Update user password
    */
    @Route(Path="/authorization/users/changepassword/{UserId}", Verbs="PUT")
    @Api(Description="Update user password")
    public static class UpdateUserPassword implements IReturn<UpdateUserPasswordResponse>
    {
        public Integer userId = null;
        public String newPassword = null;
        
        public Integer getUserId() { return userId; }
        public UpdateUserPassword setUserId(Integer value) { this.userId = value; return this; }
        public String getNewPassword() { return newPassword; }
        public UpdateUserPassword setNewPassword(String value) { this.newPassword = value; return this; }
        private static Object responseType = UpdateUserPasswordResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Create subscription
    */
    @Route(Path="/subscription", Verbs="POST")
    @Api(Description="Create subscription")
    public static class CreateSubscription implements IReturn<CreateSubscriptionResponse>
    {
        public String productId = null;
        /**
        * Application 
        */
        @ApiMember(Description="Application ", IsRequired=true, ParameterType="form")
        public String application = null;
        
        public String getProductId() { return productId; }
        public CreateSubscription setProductId(String value) { this.productId = value; return this; }
        public String getApplication() { return application; }
        public CreateSubscription setApplication(String value) { this.application = value; return this; }
        private static Object responseType = CreateSubscriptionResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Get subscription.
    */
    @Route(Path="/subscription", Verbs="GET")
    @Api(Description="Get subscription.")
    public static class GetSubscription implements IReturn<GetSubscriptionResponse>
    {
        
        private static Object responseType = GetSubscriptionResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Renew subscription
    */
    @Route(Path="/subscription/{SubscriptionId}/renew", Verbs="POST")
    @Api(Description="Renew subscription")
    public static class RenewSubscription implements IReturn<RenewSubscriptionResponse>
    {
        public String subscriptionId = null;
        public String application = null;
        public String performedBy = null;
        
        public String getSubscriptionId() { return subscriptionId; }
        public RenewSubscription setSubscriptionId(String value) { this.subscriptionId = value; return this; }
        public String getApplication() { return application; }
        public RenewSubscription setApplication(String value) { this.application = value; return this; }
        public String getPerformedBy() { return performedBy; }
        public RenewSubscription setPerformedBy(String value) { this.performedBy = value; return this; }
        private static Object responseType = RenewSubscriptionResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Change subscription
    */
    @Route(Path="/subscription/change", Verbs="POST")
    @Api(Description="Change subscription")
    public static class ChangeSubscription implements IReturn<ChangeSubscriptionResponse>
    {
        public String productId = null;
        /**
        * Application 
        */
        @ApiMember(Description="Application ", IsRequired=true, ParameterType="form")
        public String application = null;
        
        public String getProductId() { return productId; }
        public ChangeSubscription setProductId(String value) { this.productId = value; return this; }
        public String getApplication() { return application; }
        public ChangeSubscription setApplication(String value) { this.application = value; return this; }
        private static Object responseType = ChangeSubscriptionResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Activate deferred subscription
    */
    @Route(Path="/subscriptions/deferred/{SubscriptionId}/activate", Verbs="POST")
    @Api(Description="Activate deferred subscription")
    public static class ActivateDeferredSubscription implements IReturn<ActivateDeferredSubscriptionResponse>
    {
        public Integer subscriptionId = null;
        public String application = null;
        
        public Integer getSubscriptionId() { return subscriptionId; }
        public ActivateDeferredSubscription setSubscriptionId(Integer value) { this.subscriptionId = value; return this; }
        public String getApplication() { return application; }
        public ActivateDeferredSubscription setApplication(String value) { this.application = value; return this; }
        private static Object responseType = ActivateDeferredSubscriptionResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Share app.
    */
    @Route(Path="/share/app", Verbs="POST")
    @Api(Description="Share app.")
    public static class ShareApp implements IReturn<ShareAppResponse>
    {
        /**
        * Name of the user that is sharing the app
        */
        @ApiMember(Description="Name of the user that is sharing the app", ParameterType="form")
        public String senderName = null;

        /**
        * Email of phone number
        */
        @ApiMember(Description="Email of phone number", IsRequired=true, ParameterType="form")
        public ArrayList<String> contacts = null;

        /**
        * Text to share
        */
        @ApiMember(Description="Text to share", IsRequired=true, ParameterType="form")
        public String text = null;

        /**
        * Urls of the multiple platforms.
        */
        @ApiMember(Description="Urls of the multiple platforms.", IsRequired=true, ParameterType="form")
        public ArrayList<Platform> platforms = null;
        
        public String getSenderName() { return senderName; }
        public ShareApp setSenderName(String value) { this.senderName = value; return this; }
        public ArrayList<String> getContacts() { return contacts; }
        public ShareApp setContacts(ArrayList<String> value) { this.contacts = value; return this; }
        public String getText() { return text; }
        public ShareApp setText(String value) { this.text = value; return this; }
        public ArrayList<Platform> getPlatforms() { return platforms; }
        public ShareApp setPlatforms(ArrayList<Platform> value) { this.platforms = value; return this; }
        private static Object responseType = ShareAppResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Get products.
    */
    @Route(Path="/products", Verbs="GET")
    @Api(Description="Get products.")
    public static class GetProducts implements IReturn<GetProductsResponse>
    {
        
        private static Object responseType = GetProductsResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Get PayPal token.
    */
    @Route(Path="/paypal/token", Verbs="GET")
    @Api(Description="Get PayPal token.")
    public static class GetPayPalToken implements IReturn<GetPayPalTokenResponse>
    {
        
        private static Object responseType = GetPayPalTokenResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Create PayPal payment method.
    */
    @Route(Path="/paymentmethod/paypal", Verbs="POST")
    @Api(Description="Create PayPal payment method.")
    public static class CreatePayPalPaymentMethod implements IReturn<CreatePayPalPaymentMethodResponse>
    {
        public String nonce = null;
        /**
        * Application 
        */
        @ApiMember(Description="Application ", IsRequired=true, ParameterType="form")
        public String application = null;
        
        public String getNonce() { return nonce; }
        public CreatePayPalPaymentMethod setNonce(String value) { this.nonce = value; return this; }
        public String getApplication() { return application; }
        public CreatePayPalPaymentMethod setApplication(String value) { this.application = value; return this; }
        private static Object responseType = CreatePayPalPaymentMethodResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Create Credit Card payment method.
    */
    @Route(Path="/paymentmethod/creditcard", Verbs="POST")
    @Api(Description="Create Credit Card payment method.")
    public static class CreateCreditCardPaymentMethod implements IReturn<CreateCreditCardPaymentMethodResponse>
    {
        public String firstName = null;
        public String lastName = null;
        public String phoneNumber = null;
        public String address = null;
        public String city = null;
        public String stateRegion = null;
        public String postalCode = null;
        public String country = null;
        public String cardNumber = null;
        public String cardExpiry = null;
        public String ccvCode = null;
        public String accountNumber = null;
        public String userName = null;
        /**
        * Application 
        */
        @ApiMember(Description="Application ", IsRequired=true, ParameterType="form")
        public String application = null;
        
        public String getFirstName() { return firstName; }
        public CreateCreditCardPaymentMethod setFirstName(String value) { this.firstName = value; return this; }
        public String getLastName() { return lastName; }
        public CreateCreditCardPaymentMethod setLastName(String value) { this.lastName = value; return this; }
        public String getPhoneNumber() { return phoneNumber; }
        public CreateCreditCardPaymentMethod setPhoneNumber(String value) { this.phoneNumber = value; return this; }
        public String getAddress() { return address; }
        public CreateCreditCardPaymentMethod setAddress(String value) { this.address = value; return this; }
        public String getCity() { return city; }
        public CreateCreditCardPaymentMethod setCity(String value) { this.city = value; return this; }
        public String getStateRegion() { return stateRegion; }
        public CreateCreditCardPaymentMethod setStateRegion(String value) { this.stateRegion = value; return this; }
        public String getPostalCode() { return postalCode; }
        public CreateCreditCardPaymentMethod setPostalCode(String value) { this.postalCode = value; return this; }
        public String getCountry() { return country; }
        public CreateCreditCardPaymentMethod setCountry(String value) { this.country = value; return this; }
        public String getCardNumber() { return cardNumber; }
        public CreateCreditCardPaymentMethod setCardNumber(String value) { this.cardNumber = value; return this; }
        public String getCardExpiry() { return cardExpiry; }
        public CreateCreditCardPaymentMethod setCardExpiry(String value) { this.cardExpiry = value; return this; }
        public String getCcvCode() { return ccvCode; }
        public CreateCreditCardPaymentMethod setCcvCode(String value) { this.ccvCode = value; return this; }
        public String getAccountNumber() { return accountNumber; }
        public CreateCreditCardPaymentMethod setAccountNumber(String value) { this.accountNumber = value; return this; }
        public String getUserName() { return userName; }
        public CreateCreditCardPaymentMethod setUserName(String value) { this.userName = value; return this; }
        public String getApplication() { return application; }
        public CreateCreditCardPaymentMethod setApplication(String value) { this.application = value; return this; }
        private static Object responseType = CreateCreditCardPaymentMethodResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Get payment method.
    */
    @Route(Path="/paymentmethod", Verbs="GET")
    @Api(Description="Get payment method.")
    public static class GetPaymentMethod implements IReturn<GetPaymentMethodResponse>
    {
        public String accountNumber = null;
        
        public String getAccountNumber() { return accountNumber; }
        public GetPaymentMethod setAccountNumber(String value) { this.accountNumber = value; return this; }
        private static Object responseType = GetPaymentMethodResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Get news feed for backup platform.
    */
    @Route(Path="/news", Verbs="GET")
    @Api(Description="Get news feed for backup platform.")
    public static class GetNewsFeed implements IReturn<GetNewsFeedResponse>
    {
        public Integer take = null;
        public String language = null;
        
        public Integer getTake() { return take; }
        public GetNewsFeed setTake(Integer value) { this.take = value; return this; }
        public String getLanguage() { return language; }
        public GetNewsFeed setLanguage(String value) { this.language = value; return this; }
        private static Object responseType = GetNewsFeedResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Get news feed by Id
    */
    @Route(Path="/news/{Id}", Verbs="GET")
    @Api(Description="Get news feed by Id")
    public static class GetNewsFeedById implements IReturn<GetNewsFeedByIdResponse>
    {
        public String id = null;
        public String language = null;
        
        public String getId() { return id; }
        public GetNewsFeedById setId(String value) { this.id = value; return this; }
        public String getLanguage() { return language; }
        public GetNewsFeedById setLanguage(String value) { this.language = value; return this; }
        private static Object responseType = GetNewsFeedByIdResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Get the company list.
    */
    @Route(Path="/authorization/companies", Verbs="GET")
    @Api(Description="Get the company list.")
    public static class GetCompanies implements IReturn<ArrayList<CompanyModel>>
    {
        
        private static Object responseType = new TypeToken<ArrayList<CompanyModel>>(){}.getType();
        public Object getResponseType() { return responseType; }
    }

    /**
    * Get company by Id.
    */
    @Route(Path="/authorization/companies/Id", Verbs="GET")
    @Api(Description="Get company by Id.")
    public static class GetCompany implements IReturn<GetCompanyResponse>
    {
        public Integer id = null;
        
        public Integer getId() { return id; }
        public GetCompany setId(Integer value) { this.id = value; return this; }
        private static Object responseType = GetCompanyResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Create a new company.
    */
    @Route(Path="/authorization/companies", Verbs="POST")
    @Api(Description="Create a new company.")
    public static class CreateCompany implements IReturn<CreateCompanyResponse>
    {
        public String name = null;
        public String logo = null;
        public String quota = null;
        public Boolean hasAutoRegister = null;
        
        public String getName() { return name; }
        public CreateCompany setName(String value) { this.name = value; return this; }
        public String getLogo() { return logo; }
        public CreateCompany setLogo(String value) { this.logo = value; return this; }
        public String getQuota() { return quota; }
        public CreateCompany setQuota(String value) { this.quota = value; return this; }
        public Boolean isHasAutoRegister() { return hasAutoRegister; }
        public CreateCompany setHasAutoRegister(Boolean value) { this.hasAutoRegister = value; return this; }
        private static Object responseType = CreateCompanyResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Update a company.
    */
    @Route(Path="/authorization/companies", Verbs="PUT")
    @Api(Description="Update a company.")
    public static class UpdateCompany implements IReturn<UpdateCompanyResponse>
    {
        public Integer id = null;
        public String name = null;
        public String logo = null;
        public String quota = null;
        public Boolean hasAutoRegister = null;
        
        public Integer getId() { return id; }
        public UpdateCompany setId(Integer value) { this.id = value; return this; }
        public String getName() { return name; }
        public UpdateCompany setName(String value) { this.name = value; return this; }
        public String getLogo() { return logo; }
        public UpdateCompany setLogo(String value) { this.logo = value; return this; }
        public String getQuota() { return quota; }
        public UpdateCompany setQuota(String value) { this.quota = value; return this; }
        public Boolean isHasAutoRegister() { return hasAutoRegister; }
        public UpdateCompany setHasAutoRegister(Boolean value) { this.hasAutoRegister = value; return this; }
        private static Object responseType = UpdateCompanyResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Delete company.
    */
    @Route(Path="/authorization/companies/{Id}", Verbs="DELETE")
    @Api(Description="Delete company.")
    public static class DeleteCompany implements IReturn<DeleteCompanyResponse>
    {
        public Integer id = null;
        
        public Integer getId() { return id; }
        public DeleteCompany setId(Integer value) { this.id = value; return this; }
        private static Object responseType = DeleteCompanyResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Refresh access token
    */
    @Route(Path="/auth/token", Verbs="POST")
    @Api(Description="Refresh access token")
    public static class GenerateNewAccessToken implements IReturn<GenerateNewAccessTokenResponse>
    {
        public String refreshToken = null;
        
        public String getRefreshToken() { return refreshToken; }
        public GenerateNewAccessToken setRefreshToken(String value) { this.refreshToken = value; return this; }
        private static Object responseType = GenerateNewAccessTokenResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Delete all account files in the cloud.
    */
    @Route(Path="/accounts/{AccountNumber}/files", Verbs="POST")
    @Api(Description="Delete all account files in the cloud.")
    public static class DeleteFiles implements IReturn<DeleteFilesResponse>
    {
        /**
        * AccountNumber 
        */
        @ApiMember(Description="AccountNumber ", IsRequired=true, ParameterType="path")
        public String accountNumber = null;

        /**
        * Application 
        */
        @ApiMember(Description="Application ", IsRequired=true, ParameterType="form")
        public String application = null;

        /**
        * PerformedBy 
        */
        @ApiMember(Description="PerformedBy ", ParameterType="form")
        public String performedBy = null;
        
        public String getAccountNumber() { return accountNumber; }
        public DeleteFiles setAccountNumber(String value) { this.accountNumber = value; return this; }
        public String getApplication() { return application; }
        public DeleteFiles setApplication(String value) { this.application = value; return this; }
        public String getPerformedBy() { return performedBy; }
        public DeleteFiles setPerformedBy(String value) { this.performedBy = value; return this; }
        private static Object responseType = DeleteFilesResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Fetch account used storage.
    */
    @Route(Path="/accounts/{AccountNumber}/usage", Verbs="GET")
    @Api(Description="Fetch account used storage.")
    public static class GetAccountUsage implements IReturn<GetAccountUsageResponse>
    {
        /**
        * Account Number.
        */
        @ApiMember(Description="Account Number.", IsRequired=true, ParameterType="path")
        public String accountNumber = null;
        
        public String getAccountNumber() { return accountNumber; }
        public GetAccountUsage setAccountNumber(String value) { this.accountNumber = value; return this; }
        private static Object responseType = GetAccountUsageResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Fetch account used storage for logged user of the mobile app.
    */
    @Route(Path="/accounts/user/usage", Verbs="GET")
    @Api(Description="Fetch account used storage for logged user of the mobile app.")
    public static class GetUserAccountUsage implements IReturn<GetAccountUsageResponse>
    {
        
        private static Object responseType = GetAccountUsageResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Update remote storage account. Currently the Nexcloud account
    */
    @Route(Path="/accounts/{AccountNumber}/remote-account", Verbs="PUT")
    @Api(Description="Update remote storage account. Currently the Nexcloud account")
    public static class UpdateRemoteAccount implements IReturn<UpdateRemoteAccountResponse>
    {
        /**
        * AccountNumber 
        */
        @ApiMember(Description="AccountNumber ", IsRequired=true, ParameterType="form")
        public String accountNumber = null;

        /**
        * Enabled 
        */
        @ApiMember(Description="Enabled ", IsRequired=true, ParameterType="form")
        public Boolean enabled = null;

        /**
        * Application 
        */
        @ApiMember(Description="Application ", IsRequired=true, ParameterType="form")
        public String application = null;

        /**
        * PerformedBy 
        */
        @ApiMember(Description="PerformedBy ", ParameterType="form")
        public String performedBy = null;
        
        public String getAccountNumber() { return accountNumber; }
        public UpdateRemoteAccount setAccountNumber(String value) { this.accountNumber = value; return this; }
        public Boolean isEnabled() { return enabled; }
        public UpdateRemoteAccount setEnabled(Boolean value) { this.enabled = value; return this; }
        public String getApplication() { return application; }
        public UpdateRemoteAccount setApplication(String value) { this.application = value; return this; }
        public String getPerformedBy() { return performedBy; }
        public UpdateRemoteAccount setPerformedBy(String value) { this.performedBy = value; return this; }
        private static Object responseType = UpdateRemoteAccountResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Get crsf token from remote cloud system.
    */
    @Route(Path="/accounts/remote-account/csrf-token", Verbs="GET")
    @Api(Description="Get crsf token from remote cloud system.")
    public static class GetRemoteAccountCsrfToken implements IReturn<GetRemoteAccountCrsfTokenResponse>
    {
        
        private static Object responseType = GetRemoteAccountCrsfTokenResponse.class;
        public Object getResponseType() { return responseType; }
    }

    /**
    * Get devices used for file sync.
    */
    @Route(Path="/accounts/{AccountNumber}/devices", Verbs="GET")
    @Api(Description="Get devices used for file sync.")
    public static class GetDevices implements IReturn<GetDevicesResponse>
    {
        /**
        * AccountNumber 
        */
        @ApiMember(Description="AccountNumber ", IsRequired=true, ParameterType="path")
        public String accountNumber = null;
        
        public String getAccountNumber() { return accountNumber; }
        public GetDevices setAccountNumber(String value) { this.accountNumber = value; return this; }
        private static Object responseType = GetDevicesResponse.class;
        public Object getResponseType() { return responseType; }
    }

    @Route("/auth")
    // @Route("/auth/{provider}")
    // @Route("/authenticate")
    // @Route("/authenticate/{provider}")
    @DataContract
    public static class Authenticate implements IReturn<AuthenticateResponse>, IPost
    {
        @DataMember(Order=1)
        public String provider = null;

        @DataMember(Order=2)
        public String state = null;

        @DataMember(Order=3)
        public String oauth_token = null;

        @DataMember(Order=4)
        public String oauth_verifier = null;

        @DataMember(Order=5)
        public String userName = null;

        @DataMember(Order=6)
        public String password = null;

        @DataMember(Order=7)
        public Boolean rememberMe = null;

        @DataMember(Order=8)
        @SerializedName("continue") public String Continue = null;

        @DataMember(Order=9)
        public String nonce = null;

        @DataMember(Order=10)
        public String uri = null;

        @DataMember(Order=11)
        public String response = null;

        @DataMember(Order=12)
        public String qop = null;

        @DataMember(Order=13)
        public String nc = null;

        @DataMember(Order=14)
        public String cnonce = null;

        @DataMember(Order=15)
        public Boolean useTokenCookie = null;

        @DataMember(Order=16)
        public String accessToken = null;

        @DataMember(Order=17)
        public String accessTokenSecret = null;

        @DataMember(Order=18)
        public HashMap<String,String> meta = null;
        
        public String getProvider() { return provider; }
        public Authenticate setProvider(String value) { this.provider = value; return this; }
        public String getState() { return state; }
        public Authenticate setState(String value) { this.state = value; return this; }
        public String getOauthToken() { return oauth_token; }
        public Authenticate setOauthToken(String value) { this.oauth_token = value; return this; }
        public String getOauthVerifier() { return oauth_verifier; }
        public Authenticate setOauthVerifier(String value) { this.oauth_verifier = value; return this; }
        public String getUserName() { return userName; }
        public Authenticate setUserName(String value) { this.userName = value; return this; }
        public String getPassword() { return password; }
        public Authenticate setPassword(String value) { this.password = value; return this; }
        public Boolean isRememberMe() { return rememberMe; }
        public Authenticate setRememberMe(Boolean value) { this.rememberMe = value; return this; }
        public String getContinue() { return Continue; }
        public Authenticate setContinue(String value) { this.Continue = value; return this; }
        public String getNonce() { return nonce; }
        public Authenticate setNonce(String value) { this.nonce = value; return this; }
        public String getUri() { return uri; }
        public Authenticate setUri(String value) { this.uri = value; return this; }
        public String getResponse() { return response; }
        public Authenticate setResponse(String value) { this.response = value; return this; }
        public String getQop() { return qop; }
        public Authenticate setQop(String value) { this.qop = value; return this; }
        public String getNc() { return nc; }
        public Authenticate setNc(String value) { this.nc = value; return this; }
        public String getCnonce() { return cnonce; }
        public Authenticate setCnonce(String value) { this.cnonce = value; return this; }
        public Boolean isUseTokenCookie() { return useTokenCookie; }
        public Authenticate setUseTokenCookie(Boolean value) { this.useTokenCookie = value; return this; }
        public String getAccessToken() { return accessToken; }
        public Authenticate setAccessToken(String value) { this.accessToken = value; return this; }
        public String getAccessTokenSecret() { return accessTokenSecret; }
        public Authenticate setAccessTokenSecret(String value) { this.accessTokenSecret = value; return this; }
        public HashMap<String,String> getMeta() { return meta; }
        public Authenticate setMeta(HashMap<String,String> value) { this.meta = value; return this; }
        private static Object responseType = AuthenticateResponse.class;
        public Object getResponseType() { return responseType; }
    }

    @DataContract
    public static class QueryResponse<T>
    {
        @DataMember(Order=1)
        public Integer offset = null;

        @DataMember(Order=2)
        public Integer total = null;

        @DataMember(Order=3)
        public ArrayList<T> results = null;

        @DataMember(Order=4)
        public HashMap<String,String> meta = null;

        @DataMember(Order=5)
        public ResponseStatus responseStatus = null;
        
        public Integer getOffset() { return offset; }
        public QueryResponse<T> setOffset(Integer value) { this.offset = value; return this; }
        public Integer getTotal() { return total; }
        public QueryResponse<T> setTotal(Integer value) { this.total = value; return this; }
        public ArrayList<T> getResults() { return results; }
        public QueryResponse<T> setResults(ArrayList<T> value) { this.results = value; return this; }
        public HashMap<String,String> getMeta() { return meta; }
        public QueryResponse<T> setMeta(HashMap<String,String> value) { this.meta = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public QueryResponse<T> setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class GetTimeZonesResponse
    {
        public ArrayList<TimeZoneModel> timeZones = null;
        public ResponseStatus responseStatus = null;
        
        public ArrayList<TimeZoneModel> getTimeZones() { return timeZones; }
        public GetTimeZonesResponse setTimeZones(ArrayList<TimeZoneModel> value) { this.timeZones = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public GetTimeZonesResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class GetMobileAppConfigResponse
    {
        public ArrayList<String> versions = null;
        public String facebookUrl = null;
        public String callCenterPhone = null;
        public String twitterUrl = null;
        public HashMap<String,String> termsAndPrivacyUrls = null;
        
        public ArrayList<String> getVersions() { return versions; }
        public GetMobileAppConfigResponse setVersions(ArrayList<String> value) { this.versions = value; return this; }
        public String getFacebookUrl() { return facebookUrl; }
        public GetMobileAppConfigResponse setFacebookUrl(String value) { this.facebookUrl = value; return this; }
        public String getCallCenterPhone() { return callCenterPhone; }
        public GetMobileAppConfigResponse setCallCenterPhone(String value) { this.callCenterPhone = value; return this; }
        public String getTwitterUrl() { return twitterUrl; }
        public GetMobileAppConfigResponse setTwitterUrl(String value) { this.twitterUrl = value; return this; }
        public HashMap<String,String> getTermsAndPrivacyUrls() { return termsAndPrivacyUrls; }
        public GetMobileAppConfigResponse setTermsAndPrivacyUrls(HashMap<String,String> value) { this.termsAndPrivacyUrls = value; return this; }
    }

    public static class GetCountriesResponse
    {
        public ArrayList<Country> countries = null;
        public ResponseStatus responseStatus = null;
        
        public ArrayList<Country> getCountries() { return countries; }
        public GetCountriesResponse setCountries(ArrayList<Country> value) { this.countries = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public GetCountriesResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class CustomRegisterResponse
    {
        public String userId = null;
        public ResponseStatus responseStatus = null;
        
        public String getUserId() { return userId; }
        public CustomRegisterResponse setUserId(String value) { this.userId = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public CustomRegisterResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class SendPasswordResetCodeResponse
    {
        public ResponseStatus responseStatus = null;
        
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public SendPasswordResetCodeResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class PerformResetPasswordResponse
    {
        public ResponseStatus responseStatus = null;
        
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public PerformResetPasswordResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class SendPhoneVerificationCodeResponse
    {
        public ResponseStatus responseStatus = null;
        
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public SendPhoneVerificationCodeResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class ValidatePhoneVerificationCodeResponse
    {
        public String valid = null;
        public ResponseStatus responseStatus = null;
        
        public String getValid() { return valid; }
        public ValidatePhoneVerificationCodeResponse setValid(String value) { this.valid = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public ValidatePhoneVerificationCodeResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class GetFullUserResponse
    {
        public FullUser user = null;
        public ResponseStatus responseStatus = null;
        
        public FullUser getUser() { return user; }
        public GetFullUserResponse setUser(FullUser value) { this.user = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public GetFullUserResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class GetSessionResponse
    {
        public AuthUserSession result = null;
        public ResponseStatus responseStatus = null;
        
        public AuthUserSession getResult() { return result; }
        public GetSessionResponse setResult(AuthUserSession value) { this.result = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public GetSessionResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class GetUserProfileResponse
    {
        public UserModel user = null;
        public ResponseStatus responseStatus = null;
        
        public UserModel getUser() { return user; }
        public GetUserProfileResponse setUser(UserModel value) { this.user = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public GetUserProfileResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class UpdateUserProfileReponse
    {
        public ResponseStatus responseStatus = null;
        
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public UpdateUserProfileReponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class GetUsersResponse
    {
        public ArrayList<UserModel> users = null;
        public ResponseStatus responseStatus = null;
        
        public ArrayList<UserModel> getUsers() { return users; }
        public GetUsersResponse setUsers(ArrayList<UserModel> value) { this.users = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public GetUsersResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class GetUserResponse
    {
        public UserModel user = null;
        public ResponseStatus responseStatus = null;
        
        public UserModel getUser() { return user; }
        public GetUserResponse setUser(UserModel value) { this.user = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public GetUserResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class CreateUserResponse
    {
        public Integer id = null;
        public ResponseStatus responseStatus = null;
        
        public Integer getId() { return id; }
        public CreateUserResponse setId(Integer value) { this.id = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public CreateUserResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class UpdateUserResponse
    {
        public ResponseStatus responseStatus = null;
        
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public UpdateUserResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class DeleteUserResponse
    {
        public ResponseStatus responseStatus = null;
        
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public DeleteUserResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class UpdateUserPasswordResponse
    {
        public ResponseStatus responseStatus = null;
        
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public UpdateUserPasswordResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class CreateSubscriptionResponse
    {
        public Subscription subscription = null;
        public ResponseStatus responseStatus = null;
        
        public Subscription getSubscription() { return subscription; }
        public CreateSubscriptionResponse setSubscription(Subscription value) { this.subscription = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public CreateSubscriptionResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class GetSubscriptionResponse
    {
        public Product product = null;
        public Date startDate = null;
        public Date nextPaymentDate = null;
        public ResponseStatus responseStatus = null;
        
        public Product getProduct() { return product; }
        public GetSubscriptionResponse setProduct(Product value) { this.product = value; return this; }
        public Date getStartDate() { return startDate; }
        public GetSubscriptionResponse setStartDate(Date value) { this.startDate = value; return this; }
        public Date getNextPaymentDate() { return nextPaymentDate; }
        public GetSubscriptionResponse setNextPaymentDate(Date value) { this.nextPaymentDate = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public GetSubscriptionResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class RenewSubscriptionResponse
    {
        public String paymentReference = null;
        public BigDecimal chargedAmount = null;
        public Date nextExpiryDate = null;
        public ResponseStatus responseStatus = null;
        
        public String getPaymentReference() { return paymentReference; }
        public RenewSubscriptionResponse setPaymentReference(String value) { this.paymentReference = value; return this; }
        public BigDecimal getChargedAmount() { return chargedAmount; }
        public RenewSubscriptionResponse setChargedAmount(BigDecimal value) { this.chargedAmount = value; return this; }
        public Date getNextExpiryDate() { return nextExpiryDate; }
        public RenewSubscriptionResponse setNextExpiryDate(Date value) { this.nextExpiryDate = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public RenewSubscriptionResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class ChangeSubscriptionResponse
    {
        public Subscription subscription = null;
        public ResponseStatus responseStatus = null;
        
        public Subscription getSubscription() { return subscription; }
        public ChangeSubscriptionResponse setSubscription(Subscription value) { this.subscription = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public ChangeSubscriptionResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class ActivateDeferredSubscriptionResponse
    {
        public Subscription subscription = null;
        public ResponseStatus responseStatus = null;
        
        public Subscription getSubscription() { return subscription; }
        public ActivateDeferredSubscriptionResponse setSubscription(Subscription value) { this.subscription = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public ActivateDeferredSubscriptionResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class ShareAppResponse
    {
        public ResponseStatus responseStatus = null;
        
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public ShareAppResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class GetProductsResponse
    {
        public ArrayList<Product> products = null;
        public ResponseStatus responseStatus = null;
        
        public ArrayList<Product> getProducts() { return products; }
        public GetProductsResponse setProducts(ArrayList<Product> value) { this.products = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public GetProductsResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class GetPayPalTokenResponse
    {
        public String token = null;
        public ResponseStatus responseStatus = null;
        
        public String getToken() { return token; }
        public GetPayPalTokenResponse setToken(String value) { this.token = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public GetPayPalTokenResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class CreatePayPalPaymentMethodResponse
    {
        public String paymentType = null;
        public String paymentId = null;
        public ResponseStatus responseStatus = null;
        
        public String getPaymentType() { return paymentType; }
        public CreatePayPalPaymentMethodResponse setPaymentType(String value) { this.paymentType = value; return this; }
        public String getPaymentId() { return paymentId; }
        public CreatePayPalPaymentMethodResponse setPaymentId(String value) { this.paymentId = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public CreatePayPalPaymentMethodResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class CreateCreditCardPaymentMethodResponse
    {
        public String paymentType = null;
        public String paymentId = null;
        public ResponseStatus responseStatus = null;
        
        public String getPaymentType() { return paymentType; }
        public CreateCreditCardPaymentMethodResponse setPaymentType(String value) { this.paymentType = value; return this; }
        public String getPaymentId() { return paymentId; }
        public CreateCreditCardPaymentMethodResponse setPaymentId(String value) { this.paymentId = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public CreateCreditCardPaymentMethodResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class GetPaymentMethodResponse
    {
        public String paymentType = null;
        public String paymentId = null;
        public String cardNumber = null;
        public String cardType = null;
        public String expirationDate = null;
        public String payPalEmail = null;
        public String payPalImageUrl = null;
        public ResponseStatus responseStatus = null;
        
        public String getPaymentType() { return paymentType; }
        public GetPaymentMethodResponse setPaymentType(String value) { this.paymentType = value; return this; }
        public String getPaymentId() { return paymentId; }
        public GetPaymentMethodResponse setPaymentId(String value) { this.paymentId = value; return this; }
        public String getCardNumber() { return cardNumber; }
        public GetPaymentMethodResponse setCardNumber(String value) { this.cardNumber = value; return this; }
        public String getCardType() { return cardType; }
        public GetPaymentMethodResponse setCardType(String value) { this.cardType = value; return this; }
        public String getExpirationDate() { return expirationDate; }
        public GetPaymentMethodResponse setExpirationDate(String value) { this.expirationDate = value; return this; }
        public String getPayPalEmail() { return payPalEmail; }
        public GetPaymentMethodResponse setPayPalEmail(String value) { this.payPalEmail = value; return this; }
        public String getPayPalImageUrl() { return payPalImageUrl; }
        public GetPaymentMethodResponse setPayPalImageUrl(String value) { this.payPalImageUrl = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public GetPaymentMethodResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class GetNewsFeedResponse
    {
        public ArrayList<NewsFeed> newsFeeds = null;
        public ResponseStatus responseStatus = null;
        
        public ArrayList<NewsFeed> getNewsFeeds() { return newsFeeds; }
        public GetNewsFeedResponse setNewsFeeds(ArrayList<NewsFeed> value) { this.newsFeeds = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public GetNewsFeedResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class GetNewsFeedByIdResponse
    {
        public NewsFeed newsFeed = null;
        public ResponseStatus responseStatus = null;
        
        public NewsFeed getNewsFeed() { return newsFeed; }
        public GetNewsFeedByIdResponse setNewsFeed(NewsFeed value) { this.newsFeed = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public GetNewsFeedByIdResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class GetCompanyResponse
    {
        public CompanyModel company = null;
        public ResponseStatus responseStatus = null;
        
        public CompanyModel getCompany() { return company; }
        public GetCompanyResponse setCompany(CompanyModel value) { this.company = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public GetCompanyResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class CreateCompanyResponse
    {
        public ResponseStatus responseStatus = null;
        
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public CreateCompanyResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class UpdateCompanyResponse
    {
        public ResponseStatus responseStatus = null;
        
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public UpdateCompanyResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class DeleteCompanyResponse
    {
        public ResponseStatus responseStatus = null;
        
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public DeleteCompanyResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class GenerateNewAccessTokenResponse
    {
        public Token accessToken = null;
        
        public Token getAccessToken() { return accessToken; }
        public GenerateNewAccessTokenResponse setAccessToken(Token value) { this.accessToken = value; return this; }
    }

    public static class DeleteFilesResponse
    {
        public ResponseStatus responseStatus = null;
        
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public DeleteFilesResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class GetAccountUsageResponse
    {
        public Double totalQuota = null;
        public Double usedQuota = null;
        public Double availableQuota = null;
        public String unitOfMeasure = null;
        public ArrayList<UsedStorage> usedStorage = null;
        public ResponseStatus responseStatus = null;
        
        public Double getTotalQuota() { return totalQuota; }
        public GetAccountUsageResponse setTotalQuota(Double value) { this.totalQuota = value; return this; }
        public Double getUsedQuota() { return usedQuota; }
        public GetAccountUsageResponse setUsedQuota(Double value) { this.usedQuota = value; return this; }
        public Double getAvailableQuota() { return availableQuota; }
        public GetAccountUsageResponse setAvailableQuota(Double value) { this.availableQuota = value; return this; }
        public String getUnitOfMeasure() { return unitOfMeasure; }
        public GetAccountUsageResponse setUnitOfMeasure(String value) { this.unitOfMeasure = value; return this; }
        public ArrayList<UsedStorage> getUsedStorage() { return usedStorage; }
        public GetAccountUsageResponse setUsedStorage(ArrayList<UsedStorage> value) { this.usedStorage = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public GetAccountUsageResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class UpdateRemoteAccountResponse
    {
        public ResponseStatus responseStatus = null;
        
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public UpdateRemoteAccountResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    public static class GetRemoteAccountCrsfTokenResponse
    {
        public String token = null;
        
        public String getToken() { return token; }
        public GetRemoteAccountCrsfTokenResponse setToken(String value) { this.token = value; return this; }
    }

    public static class GetDevicesResponse
    {
        public ArrayList<Device> devices = null;
        public ResponseStatus responseStatus = null;
        
        public ArrayList<Device> getDevices() { return devices; }
        public GetDevicesResponse setDevices(ArrayList<Device> value) { this.devices = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public GetDevicesResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
    }

    @DataContract
    public static class AuthenticateResponse
    {
        @DataMember(Order=1)
        public String userId = null;

        @DataMember(Order=2)
        public String sessionId = null;

        @DataMember(Order=3)
        public String userName = null;

        @DataMember(Order=4)
        public String displayName = null;

        @DataMember(Order=5)
        public String referrerUrl = null;

        @DataMember(Order=6)
        public String bearerToken = null;

        @DataMember(Order=7)
        public String refreshToken = null;

        @DataMember(Order=8)
        public ResponseStatus responseStatus = null;

        @DataMember(Order=9)
        public HashMap<String,String> meta = null;
        
        public String getUserId() { return userId; }
        public AuthenticateResponse setUserId(String value) { this.userId = value; return this; }
        public String getSessionId() { return sessionId; }
        public AuthenticateResponse setSessionId(String value) { this.sessionId = value; return this; }
        public String getUserName() { return userName; }
        public AuthenticateResponse setUserName(String value) { this.userName = value; return this; }
        public String getDisplayName() { return displayName; }
        public AuthenticateResponse setDisplayName(String value) { this.displayName = value; return this; }
        public String getReferrerUrl() { return referrerUrl; }
        public AuthenticateResponse setReferrerUrl(String value) { this.referrerUrl = value; return this; }
        public String getBearerToken() { return bearerToken; }
        public AuthenticateResponse setBearerToken(String value) { this.bearerToken = value; return this; }
        public String getRefreshToken() { return refreshToken; }
        public AuthenticateResponse setRefreshToken(String value) { this.refreshToken = value; return this; }
        public ResponseStatus getResponseStatus() { return responseStatus; }
        public AuthenticateResponse setResponseStatus(ResponseStatus value) { this.responseStatus = value; return this; }
        public HashMap<String,String> getMeta() { return meta; }
        public AuthenticateResponse setMeta(HashMap<String,String> value) { this.meta = value; return this; }
    }

    public static class QueryDb<T> extends QueryBase
    {
        
    }

    public static class UserLoginHistory
    {
        public Integer id = null;
        public Integer userId = null;
        public String browserName = null;
        public String browserVersion = null;
        public String operatingSystem = null;
        public String ip = null;
        public Date dateTime = null;
        public String userAgent = null;
        public String countryCode = null;
        public String stateRegion = null;
        public String city = null;
        public String postalCode = null;
        
        public Integer getId() { return id; }
        public UserLoginHistory setId(Integer value) { this.id = value; return this; }
        public Integer getUserId() { return userId; }
        public UserLoginHistory setUserId(Integer value) { this.userId = value; return this; }
        public String getBrowserName() { return browserName; }
        public UserLoginHistory setBrowserName(String value) { this.browserName = value; return this; }
        public String getBrowserVersion() { return browserVersion; }
        public UserLoginHistory setBrowserVersion(String value) { this.browserVersion = value; return this; }
        public String getOperatingSystem() { return operatingSystem; }
        public UserLoginHistory setOperatingSystem(String value) { this.operatingSystem = value; return this; }
        public String getIp() { return ip; }
        public UserLoginHistory setIp(String value) { this.ip = value; return this; }
        public Date getDateTime() { return dateTime; }
        public UserLoginHistory setDateTime(Date value) { this.dateTime = value; return this; }
        public String getUserAgent() { return userAgent; }
        public UserLoginHistory setUserAgent(String value) { this.userAgent = value; return this; }
        public String getCountryCode() { return countryCode; }
        public UserLoginHistory setCountryCode(String value) { this.countryCode = value; return this; }
        public String getStateRegion() { return stateRegion; }
        public UserLoginHistory setStateRegion(String value) { this.stateRegion = value; return this; }
        public String getCity() { return city; }
        public UserLoginHistory setCity(String value) { this.city = value; return this; }
        public String getPostalCode() { return postalCode; }
        public UserLoginHistory setPostalCode(String value) { this.postalCode = value; return this; }
    }

    public static class UserLoginMeta
    {
        public Integer id = null;
        public Integer userId = null;
        public Long userLoginHistoryId = null;
        public String metaGroup = null;
        public String metaKey = null;
        public String metaValue = null;
        public Date entryDateTime = null;
        
        public Integer getId() { return id; }
        public UserLoginMeta setId(Integer value) { this.id = value; return this; }
        public Integer getUserId() { return userId; }
        public UserLoginMeta setUserId(Integer value) { this.userId = value; return this; }
        public Long getUserLoginHistoryId() { return userLoginHistoryId; }
        public UserLoginMeta setUserLoginHistoryId(Long value) { this.userLoginHistoryId = value; return this; }
        public String getMetaGroup() { return metaGroup; }
        public UserLoginMeta setMetaGroup(String value) { this.metaGroup = value; return this; }
        public String getMetaKey() { return metaKey; }
        public UserLoginMeta setMetaKey(String value) { this.metaKey = value; return this; }
        public String getMetaValue() { return metaValue; }
        public UserLoginMeta setMetaValue(String value) { this.metaValue = value; return this; }
        public Date getEntryDateTime() { return entryDateTime; }
        public UserLoginMeta setEntryDateTime(Date value) { this.entryDateTime = value; return this; }
    }

    public static class TimeZoneModel
    {
        public String id = null;
        public String value = null;
        
        public String getId() { return id; }
        public TimeZoneModel setId(String value) { this.id = value; return this; }
        public String getValue() { return value; }
        public TimeZoneModel setValue(String value) { this.value = value; return this; }
    }

    public static class Country
    {
        public String name = null;
        public String alpha2 = null;
        public String alpha3 = null;
        public String phoneCode = null;
        
        public String getName() { return name; }
        public Country setName(String value) { this.name = value; return this; }
        public String getAlpha2() { return alpha2; }
        public Country setAlpha2(String value) { this.alpha2 = value; return this; }
        public String getAlpha3() { return alpha3; }
        public Country setAlpha3(String value) { this.alpha3 = value; return this; }
        public String getPhoneCode() { return phoneCode; }
        public Country setPhoneCode(String value) { this.phoneCode = value; return this; }
    }

    public static class FullUser
    {
        public String id = null;
        public String userName = null;
        public String email = null;
        public String countryCallingCode = null;
        public String phoneNumber = null;
        public String firstName = null;
        public String lastName = null;
        public String company = null;
        public Date birthDate = null;
        public String address = null;
        public String address2 = null;
        public String city = null;
        public String state = null;
        public String country = null;
        public String culture = null;
        public String fullName = null;
        public String gender = null;
        public String language = null;
        public String mailAddress = null;
        public String nickname = null;
        public String postalCode = null;
        public String timeZone = null;
        public Date lastLoginAttempt = null;
        public Date lockedDate = null;
        public Boolean phoneNumberVerified = null;
        
        public String getId() { return id; }
        public FullUser setId(String value) { this.id = value; return this; }
        public String getUserName() { return userName; }
        public FullUser setUserName(String value) { this.userName = value; return this; }
        public String getEmail() { return email; }
        public FullUser setEmail(String value) { this.email = value; return this; }
        public String getCountryCallingCode() { return countryCallingCode; }
        public FullUser setCountryCallingCode(String value) { this.countryCallingCode = value; return this; }
        public String getPhoneNumber() { return phoneNumber; }
        public FullUser setPhoneNumber(String value) { this.phoneNumber = value; return this; }
        public String getFirstName() { return firstName; }
        public FullUser setFirstName(String value) { this.firstName = value; return this; }
        public String getLastName() { return lastName; }
        public FullUser setLastName(String value) { this.lastName = value; return this; }
        public String getCompany() { return company; }
        public FullUser setCompany(String value) { this.company = value; return this; }
        public Date getBirthDate() { return birthDate; }
        public FullUser setBirthDate(Date value) { this.birthDate = value; return this; }
        public String getAddress() { return address; }
        public FullUser setAddress(String value) { this.address = value; return this; }
        public String getAddress2() { return address2; }
        public FullUser setAddress2(String value) { this.address2 = value; return this; }
        public String getCity() { return city; }
        public FullUser setCity(String value) { this.city = value; return this; }
        public String getState() { return state; }
        public FullUser setState(String value) { this.state = value; return this; }
        public String getCountry() { return country; }
        public FullUser setCountry(String value) { this.country = value; return this; }
        public String getCulture() { return culture; }
        public FullUser setCulture(String value) { this.culture = value; return this; }
        public String getFullName() { return fullName; }
        public FullUser setFullName(String value) { this.fullName = value; return this; }
        public String getGender() { return gender; }
        public FullUser setGender(String value) { this.gender = value; return this; }
        public String getLanguage() { return language; }
        public FullUser setLanguage(String value) { this.language = value; return this; }
        public String getMailAddress() { return mailAddress; }
        public FullUser setMailAddress(String value) { this.mailAddress = value; return this; }
        public String getNickname() { return nickname; }
        public FullUser setNickname(String value) { this.nickname = value; return this; }
        public String getPostalCode() { return postalCode; }
        public FullUser setPostalCode(String value) { this.postalCode = value; return this; }
        public String getTimeZone() { return timeZone; }
        public FullUser setTimeZone(String value) { this.timeZone = value; return this; }
        public Date getLastLoginAttempt() { return lastLoginAttempt; }
        public FullUser setLastLoginAttempt(Date value) { this.lastLoginAttempt = value; return this; }
        public Date getLockedDate() { return lockedDate; }
        public FullUser setLockedDate(Date value) { this.lockedDate = value; return this; }
        public Boolean isPhoneNumberVerified() { return phoneNumberVerified; }
        public FullUser setPhoneNumberVerified(Boolean value) { this.phoneNumberVerified = value; return this; }
    }

    @DataContract
    public static class AuthUserSession
    {
        @DataMember(Order=1)
        public String referrerUrl = null;

        @DataMember(Order=2)
        public String id = null;

        @DataMember(Order=3)
        public String userAuthId = null;

        @DataMember(Order=4)
        public String userAuthName = null;

        @DataMember(Order=5)
        public String userName = null;

        @DataMember(Order=6)
        public String twitterUserId = null;

        @DataMember(Order=7)
        public String twitterScreenName = null;

        @DataMember(Order=8)
        public String facebookUserId = null;

        @DataMember(Order=9)
        public String facebookUserName = null;

        @DataMember(Order=10)
        public String firstName = null;

        @DataMember(Order=11)
        public String lastName = null;

        @DataMember(Order=12)
        public String displayName = null;

        @DataMember(Order=13)
        public String company = null;

        @DataMember(Order=14)
        public String email = null;

        @DataMember(Order=15)
        public String primaryEmail = null;

        @DataMember(Order=16)
        public String phoneNumber = null;

        @DataMember(Order=17)
        public Date birthDate = null;

        @DataMember(Order=18)
        public String birthDateRaw = null;

        @DataMember(Order=19)
        public String address = null;

        @DataMember(Order=20)
        public String address2 = null;

        @DataMember(Order=21)
        public String city = null;

        @DataMember(Order=22)
        public String state = null;

        @DataMember(Order=23)
        public String country = null;

        @DataMember(Order=24)
        public String culture = null;

        @DataMember(Order=25)
        public String fullName = null;

        @DataMember(Order=26)
        public String gender = null;

        @DataMember(Order=27)
        public String language = null;

        @DataMember(Order=28)
        public String mailAddress = null;

        @DataMember(Order=29)
        public String nickname = null;

        @DataMember(Order=30)
        public String postalCode = null;

        @DataMember(Order=31)
        public String timeZone = null;

        @DataMember(Order=32)
        public String requestTokenSecret = null;

        @DataMember(Order=33)
        public Date createdAt = null;

        @DataMember(Order=34)
        public Date lastModified = null;

        @DataMember(Order=35)
        public ArrayList<String> roles = null;

        @DataMember(Order=36)
        public ArrayList<String> permissions = null;

        @DataMember(Order=37)
        public Boolean isAuthenticated = null;

        @DataMember(Order=38)
        public Boolean fromToken = null;

        @DataMember(Order=39)
        public String profileUrl = null;

        @DataMember(Order=40)
        public String sequence = null;

        @DataMember(Order=41)
        public Long tag = null;

        @DataMember(Order=42)
        public String authProvider = null;

        @DataMember(Order=43)
        public ArrayList<IAuthTokens> providerOAuthAccess = null;
        
        public String getReferrerUrl() { return referrerUrl; }
        public AuthUserSession setReferrerUrl(String value) { this.referrerUrl = value; return this; }
        public String getId() { return id; }
        public AuthUserSession setId(String value) { this.id = value; return this; }
        public String getUserAuthId() { return userAuthId; }
        public AuthUserSession setUserAuthId(String value) { this.userAuthId = value; return this; }
        public String getUserAuthName() { return userAuthName; }
        public AuthUserSession setUserAuthName(String value) { this.userAuthName = value; return this; }
        public String getUserName() { return userName; }
        public AuthUserSession setUserName(String value) { this.userName = value; return this; }
        public String getTwitterUserId() { return twitterUserId; }
        public AuthUserSession setTwitterUserId(String value) { this.twitterUserId = value; return this; }
        public String getTwitterScreenName() { return twitterScreenName; }
        public AuthUserSession setTwitterScreenName(String value) { this.twitterScreenName = value; return this; }
        public String getFacebookUserId() { return facebookUserId; }
        public AuthUserSession setFacebookUserId(String value) { this.facebookUserId = value; return this; }
        public String getFacebookUserName() { return facebookUserName; }
        public AuthUserSession setFacebookUserName(String value) { this.facebookUserName = value; return this; }
        public String getFirstName() { return firstName; }
        public AuthUserSession setFirstName(String value) { this.firstName = value; return this; }
        public String getLastName() { return lastName; }
        public AuthUserSession setLastName(String value) { this.lastName = value; return this; }
        public String getDisplayName() { return displayName; }
        public AuthUserSession setDisplayName(String value) { this.displayName = value; return this; }
        public String getCompany() { return company; }
        public AuthUserSession setCompany(String value) { this.company = value; return this; }
        public String getEmail() { return email; }
        public AuthUserSession setEmail(String value) { this.email = value; return this; }
        public String getPrimaryEmail() { return primaryEmail; }
        public AuthUserSession setPrimaryEmail(String value) { this.primaryEmail = value; return this; }
        public String getPhoneNumber() { return phoneNumber; }
        public AuthUserSession setPhoneNumber(String value) { this.phoneNumber = value; return this; }
        public Date getBirthDate() { return birthDate; }
        public AuthUserSession setBirthDate(Date value) { this.birthDate = value; return this; }
        public String getBirthDateRaw() { return birthDateRaw; }
        public AuthUserSession setBirthDateRaw(String value) { this.birthDateRaw = value; return this; }
        public String getAddress() { return address; }
        public AuthUserSession setAddress(String value) { this.address = value; return this; }
        public String getAddress2() { return address2; }
        public AuthUserSession setAddress2(String value) { this.address2 = value; return this; }
        public String getCity() { return city; }
        public AuthUserSession setCity(String value) { this.city = value; return this; }
        public String getState() { return state; }
        public AuthUserSession setState(String value) { this.state = value; return this; }
        public String getCountry() { return country; }
        public AuthUserSession setCountry(String value) { this.country = value; return this; }
        public String getCulture() { return culture; }
        public AuthUserSession setCulture(String value) { this.culture = value; return this; }
        public String getFullName() { return fullName; }
        public AuthUserSession setFullName(String value) { this.fullName = value; return this; }
        public String getGender() { return gender; }
        public AuthUserSession setGender(String value) { this.gender = value; return this; }
        public String getLanguage() { return language; }
        public AuthUserSession setLanguage(String value) { this.language = value; return this; }
        public String getMailAddress() { return mailAddress; }
        public AuthUserSession setMailAddress(String value) { this.mailAddress = value; return this; }
        public String getNickname() { return nickname; }
        public AuthUserSession setNickname(String value) { this.nickname = value; return this; }
        public String getPostalCode() { return postalCode; }
        public AuthUserSession setPostalCode(String value) { this.postalCode = value; return this; }
        public String getTimeZone() { return timeZone; }
        public AuthUserSession setTimeZone(String value) { this.timeZone = value; return this; }
        public String getRequestTokenSecret() { return requestTokenSecret; }
        public AuthUserSession setRequestTokenSecret(String value) { this.requestTokenSecret = value; return this; }
        public Date getCreatedAt() { return createdAt; }
        public AuthUserSession setCreatedAt(Date value) { this.createdAt = value; return this; }
        public Date getLastModified() { return lastModified; }
        public AuthUserSession setLastModified(Date value) { this.lastModified = value; return this; }
        public ArrayList<String> getRoles() { return roles; }
        public AuthUserSession setRoles(ArrayList<String> value) { this.roles = value; return this; }
        public ArrayList<String> getPermissions() { return permissions; }
        public AuthUserSession setPermissions(ArrayList<String> value) { this.permissions = value; return this; }
        public Boolean getIsAuthenticated() { return isAuthenticated; }
        public AuthUserSession setIsAuthenticated(Boolean value) { this.isAuthenticated = value; return this; }
        public Boolean isFromToken() { return fromToken; }
        public AuthUserSession setFromToken(Boolean value) { this.fromToken = value; return this; }
        public String getProfileUrl() { return profileUrl; }
        public AuthUserSession setProfileUrl(String value) { this.profileUrl = value; return this; }
        public String getSequence() { return sequence; }
        public AuthUserSession setSequence(String value) { this.sequence = value; return this; }
        public Long getTag() { return tag; }
        public AuthUserSession setTag(Long value) { this.tag = value; return this; }
        public String getAuthProvider() { return authProvider; }
        public AuthUserSession setAuthProvider(String value) { this.authProvider = value; return this; }
        public ArrayList<IAuthTokens> getProviderOAuthAccess() { return providerOAuthAccess; }
        public AuthUserSession setProviderOAuthAccess(ArrayList<IAuthTokens> value) { this.providerOAuthAccess = value; return this; }
    }

    public static class UserModel extends UserAuth
    {
        public Integer companyId = null;
        
        public Integer getCompanyId() { return companyId; }
        public UserModel setCompanyId(Integer value) { this.companyId = value; return this; }
    }

    public static class Subscription
    {
        public String accountNumber = null;
        public String productId = null;
        public Date startDate = null;
        public Date nextExpiryDate = null;
        public Boolean autoRenew = null;
        public String status = null;
        
        public String getAccountNumber() { return accountNumber; }
        public Subscription setAccountNumber(String value) { this.accountNumber = value; return this; }
        public String getProductId() { return productId; }
        public Subscription setProductId(String value) { this.productId = value; return this; }
        public Date getStartDate() { return startDate; }
        public Subscription setStartDate(Date value) { this.startDate = value; return this; }
        public Date getNextExpiryDate() { return nextExpiryDate; }
        public Subscription setNextExpiryDate(Date value) { this.nextExpiryDate = value; return this; }
        public Boolean isAutoRenew() { return autoRenew; }
        public Subscription setAutoRenew(Boolean value) { this.autoRenew = value; return this; }
        public String getStatus() { return status; }
        public Subscription setStatus(String value) { this.status = value; return this; }
    }

    public static class Product
    {
        public String productId = null;
        public String name = null;
        public String periodicity = null;
        public BigDecimal price = null;
        public Float storageSize = null;
        public String storageUnit = null;
        
        public String getProductId() { return productId; }
        public Product setProductId(String value) { this.productId = value; return this; }
        public String getName() { return name; }
        public Product setName(String value) { this.name = value; return this; }
        public String getPeriodicity() { return periodicity; }
        public Product setPeriodicity(String value) { this.periodicity = value; return this; }
        public BigDecimal getPrice() { return price; }
        public Product setPrice(BigDecimal value) { this.price = value; return this; }
        public Float getStorageSize() { return storageSize; }
        public Product setStorageSize(Float value) { this.storageSize = value; return this; }
        public String getStorageUnit() { return storageUnit; }
        public Product setStorageUnit(String value) { this.storageUnit = value; return this; }
    }

    public static class Platform
    {
        public String platformName = null;
        public String url = null;
        
        public String getPlatformName() { return platformName; }
        public Platform setPlatformName(String value) { this.platformName = value; return this; }
        public String getUrl() { return url; }
        public Platform setUrl(String value) { this.url = value; return this; }
    }

    public static class NewsFeed
    {
        public Integer id = null;
        public Date creationDate = null;
        public Date updateDate = null;
        public String title = null;
        public String shortDescription = null;
        public String description = null;
        
        public Integer getId() { return id; }
        public NewsFeed setId(Integer value) { this.id = value; return this; }
        public Date getCreationDate() { return creationDate; }
        public NewsFeed setCreationDate(Date value) { this.creationDate = value; return this; }
        public Date getUpdateDate() { return updateDate; }
        public NewsFeed setUpdateDate(Date value) { this.updateDate = value; return this; }
        public String getTitle() { return title; }
        public NewsFeed setTitle(String value) { this.title = value; return this; }
        public String getShortDescription() { return shortDescription; }
        public NewsFeed setShortDescription(String value) { this.shortDescription = value; return this; }
        public String getDescription() { return description; }
        public NewsFeed setDescription(String value) { this.description = value; return this; }
    }

    public static class CompanyModel
    {
        public Integer id = null;
        public String name = null;
        public String logo = null;
        public Boolean enabled = null;
        public ArrayList<UserModel> users = null;
        public HashMap<String,Integer> totals = null;
        public String quota = null;
        public Boolean hasAutoRegister = null;
        
        public Integer getId() { return id; }
        public CompanyModel setId(Integer value) { this.id = value; return this; }
        public String getName() { return name; }
        public CompanyModel setName(String value) { this.name = value; return this; }
        public String getLogo() { return logo; }
        public CompanyModel setLogo(String value) { this.logo = value; return this; }
        public Boolean isEnabled() { return enabled; }
        public CompanyModel setEnabled(Boolean value) { this.enabled = value; return this; }
        public ArrayList<UserModel> getUsers() { return users; }
        public CompanyModel setUsers(ArrayList<UserModel> value) { this.users = value; return this; }
        public HashMap<String,Integer> getTotals() { return totals; }
        public CompanyModel setTotals(HashMap<String,Integer> value) { this.totals = value; return this; }
        public String getQuota() { return quota; }
        public CompanyModel setQuota(String value) { this.quota = value; return this; }
        public Boolean isHasAutoRegister() { return hasAutoRegister; }
        public CompanyModel setHasAutoRegister(Boolean value) { this.hasAutoRegister = value; return this; }
    }

    public static class Token
    {
        public String accessToken = null;
        public String tokenType = null;
        public Long expiresIn = null;
        public String refreshToken = null;
        public String userId = null;
        
        public String getAccessToken() { return accessToken; }
        public Token setAccessToken(String value) { this.accessToken = value; return this; }
        public String getTokenType() { return tokenType; }
        public Token setTokenType(String value) { this.tokenType = value; return this; }
        public Long getExpiresIn() { return expiresIn; }
        public Token setExpiresIn(Long value) { this.expiresIn = value; return this; }
        public String getRefreshToken() { return refreshToken; }
        public Token setRefreshToken(String value) { this.refreshToken = value; return this; }
        public String getUserId() { return userId; }
        public Token setUserId(String value) { this.userId = value; return this; }
    }

    public static class UsedStorage
    {
        public String name = null;
        public Double usedStorageSize = null;
        public String usedStorageUnit = null;
        
        public String getName() { return name; }
        public UsedStorage setName(String value) { this.name = value; return this; }
        public Double getUsedStorageSize() { return usedStorageSize; }
        public UsedStorage setUsedStorageSize(Double value) { this.usedStorageSize = value; return this; }
        public String getUsedStorageUnit() { return usedStorageUnit; }
        public UsedStorage setUsedStorageUnit(String value) { this.usedStorageUnit = value; return this; }
    }

    public static class Device
    {
        public String os = null;
        public String osVersion = null;
        public String model = null;
        public String trademark = null;
        public Date lastLogin = null;
        public String appVersion = null;
        public String deviceType = null;
        
        public String getOs() { return os; }
        public Device setOs(String value) { this.os = value; return this; }
        public String getOsVersion() { return osVersion; }
        public Device setOsVersion(String value) { this.osVersion = value; return this; }
        public String getModel() { return model; }
        public Device setModel(String value) { this.model = value; return this; }
        public String getTrademark() { return trademark; }
        public Device setTrademark(String value) { this.trademark = value; return this; }
        public Date getLastLogin() { return lastLogin; }
        public Device setLastLogin(Date value) { this.lastLogin = value; return this; }
        public String getAppVersion() { return appVersion; }
        public Device setAppVersion(String value) { this.appVersion = value; return this; }
        public String getDeviceType() { return deviceType; }
        public Device setDeviceType(String value) { this.deviceType = value; return this; }
    }

    public static class QueryBase
    {
        @DataMember(Order=1)
        public Integer skip = null;

        @DataMember(Order=2)
        public Integer take = null;

        @DataMember(Order=3)
        public String orderBy = null;

        @DataMember(Order=4)
        public String orderByDesc = null;

        @DataMember(Order=5)
        public String include = null;

        @DataMember(Order=6)
        public String fields = null;

        @DataMember(Order=7)
        public HashMap<String,String> meta = null;
        
        public Integer getSkip() { return skip; }
        public QueryBase setSkip(Integer value) { this.skip = value; return this; }
        public Integer getTake() { return take; }
        public QueryBase setTake(Integer value) { this.take = value; return this; }
        public String getOrderBy() { return orderBy; }
        public QueryBase setOrderBy(String value) { this.orderBy = value; return this; }
        public String getOrderByDesc() { return orderByDesc; }
        public QueryBase setOrderByDesc(String value) { this.orderByDesc = value; return this; }
        public String getInclude() { return include; }
        public QueryBase setInclude(String value) { this.include = value; return this; }
        public String getFields() { return fields; }
        public QueryBase setFields(String value) { this.fields = value; return this; }
        public HashMap<String,String> getMeta() { return meta; }
        public QueryBase setMeta(HashMap<String,String> value) { this.meta = value; return this; }
    }

    public static interface IAuthTokens
    {
        public String provider = null;
        public String userId = null;
        public String accessToken = null;
        public String accessTokenSecret = null;
        public String refreshToken = null;
        public Date refreshTokenExpiry = null;
        public String requestToken = null;
        public String requestTokenSecret = null;
        public HashMap<String,String> items = null;
    }

    public static class UserAuth
    {
        public Integer id = null;
        public String userName = null;
        public String email = null;
        public String primaryEmail = null;
        public String phoneNumber = null;
        public String firstName = null;
        public String lastName = null;
        public String displayName = null;
        public String company = null;
        public Date birthDate = null;
        public String birthDateRaw = null;
        public String address = null;
        public String address2 = null;
        public String city = null;
        public String state = null;
        public String country = null;
        public String culture = null;
        public String fullName = null;
        public String gender = null;
        public String language = null;
        public String mailAddress = null;
        public String nickname = null;
        public String postalCode = null;
        public String timeZone = null;
        public String salt = null;
        public String passwordHash = null;
        public String digestHa1Hash = null;
        public ArrayList<String> roles = null;
        public ArrayList<String> permissions = null;
        public Date createdDate = null;
        public Date modifiedDate = null;
        public Integer invalidLoginAttempts = null;
        public Date lastLoginAttempt = null;
        public Date lockedDate = null;
        public String recoveryToken = null;
        public Integer refId = null;
        public String refIdStr = null;
        public HashMap<String,String> meta = null;
        
        public Integer getId() { return id; }
        public UserAuth setId(Integer value) { this.id = value; return this; }
        public String getUserName() { return userName; }
        public UserAuth setUserName(String value) { this.userName = value; return this; }
        public String getEmail() { return email; }
        public UserAuth setEmail(String value) { this.email = value; return this; }
        public String getPrimaryEmail() { return primaryEmail; }
        public UserAuth setPrimaryEmail(String value) { this.primaryEmail = value; return this; }
        public String getPhoneNumber() { return phoneNumber; }
        public UserAuth setPhoneNumber(String value) { this.phoneNumber = value; return this; }
        public String getFirstName() { return firstName; }
        public UserAuth setFirstName(String value) { this.firstName = value; return this; }
        public String getLastName() { return lastName; }
        public UserAuth setLastName(String value) { this.lastName = value; return this; }
        public String getDisplayName() { return displayName; }
        public UserAuth setDisplayName(String value) { this.displayName = value; return this; }
        public String getCompany() { return company; }
        public UserAuth setCompany(String value) { this.company = value; return this; }
        public Date getBirthDate() { return birthDate; }
        public UserAuth setBirthDate(Date value) { this.birthDate = value; return this; }
        public String getBirthDateRaw() { return birthDateRaw; }
        public UserAuth setBirthDateRaw(String value) { this.birthDateRaw = value; return this; }
        public String getAddress() { return address; }
        public UserAuth setAddress(String value) { this.address = value; return this; }
        public String getAddress2() { return address2; }
        public UserAuth setAddress2(String value) { this.address2 = value; return this; }
        public String getCity() { return city; }
        public UserAuth setCity(String value) { this.city = value; return this; }
        public String getState() { return state; }
        public UserAuth setState(String value) { this.state = value; return this; }
        public String getCountry() { return country; }
        public UserAuth setCountry(String value) { this.country = value; return this; }
        public String getCulture() { return culture; }
        public UserAuth setCulture(String value) { this.culture = value; return this; }
        public String getFullName() { return fullName; }
        public UserAuth setFullName(String value) { this.fullName = value; return this; }
        public String getGender() { return gender; }
        public UserAuth setGender(String value) { this.gender = value; return this; }
        public String getLanguage() { return language; }
        public UserAuth setLanguage(String value) { this.language = value; return this; }
        public String getMailAddress() { return mailAddress; }
        public UserAuth setMailAddress(String value) { this.mailAddress = value; return this; }
        public String getNickname() { return nickname; }
        public UserAuth setNickname(String value) { this.nickname = value; return this; }
        public String getPostalCode() { return postalCode; }
        public UserAuth setPostalCode(String value) { this.postalCode = value; return this; }
        public String getTimeZone() { return timeZone; }
        public UserAuth setTimeZone(String value) { this.timeZone = value; return this; }
        public String getSalt() { return salt; }
        public UserAuth setSalt(String value) { this.salt = value; return this; }
        public String getPasswordHash() { return passwordHash; }
        public UserAuth setPasswordHash(String value) { this.passwordHash = value; return this; }
        public String getDigestHa1Hash() { return digestHa1Hash; }
        public UserAuth setDigestHa1Hash(String value) { this.digestHa1Hash = value; return this; }
        public ArrayList<String> getRoles() { return roles; }
        public UserAuth setRoles(ArrayList<String> value) { this.roles = value; return this; }
        public ArrayList<String> getPermissions() { return permissions; }
        public UserAuth setPermissions(ArrayList<String> value) { this.permissions = value; return this; }
        public Date getCreatedDate() { return createdDate; }
        public UserAuth setCreatedDate(Date value) { this.createdDate = value; return this; }
        public Date getModifiedDate() { return modifiedDate; }
        public UserAuth setModifiedDate(Date value) { this.modifiedDate = value; return this; }
        public Integer getInvalidLoginAttempts() { return invalidLoginAttempts; }
        public UserAuth setInvalidLoginAttempts(Integer value) { this.invalidLoginAttempts = value; return this; }
        public Date getLastLoginAttempt() { return lastLoginAttempt; }
        public UserAuth setLastLoginAttempt(Date value) { this.lastLoginAttempt = value; return this; }
        public Date getLockedDate() { return lockedDate; }
        public UserAuth setLockedDate(Date value) { this.lockedDate = value; return this; }
        public String getRecoveryToken() { return recoveryToken; }
        public UserAuth setRecoveryToken(String value) { this.recoveryToken = value; return this; }
        public Integer getRefId() { return refId; }
        public UserAuth setRefId(Integer value) { this.refId = value; return this; }
        public String getRefIdStr() { return refIdStr; }
        public UserAuth setRefIdStr(String value) { this.refIdStr = value; return this; }
        public HashMap<String,String> getMeta() { return meta; }
        public UserAuth setMeta(HashMap<String,String> value) { this.meta = value; return this; }
    }

}
