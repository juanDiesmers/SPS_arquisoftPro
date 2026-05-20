FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY SaludPay.Api.csproj ./
RUN dotnet restore ./SaludPay.Api.csproj
COPY . ./
RUN dotnet publish ./SaludPay.Api.csproj -c Release -o /app/publish

FROM mcr.microsoft.com/dotnet/aspnet:8.0
WORKDIR /app
COPY --from=build /app/publish .
EXPOSE 8086
ENTRYPOINT ["dotnet", "SaludPay.Api.dll"]
