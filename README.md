# TMDB App — Android nativo

Aplicativo em Kotlin e Jetpack Compose para explorar filmes populares, pesquisar títulos, consultar detalhes e manter favoritos no dispositivo. Projeto de portfólio com foco em paginação, persistência local e fluxos reativos.

## Telas

| Populares | Busca | Detalhes |
|---|---|---|
| ![Populares](screenshots/home.png) | ![Busca](screenshots/search_results.png) | ![Detalhes](screenshots/details.png) |

As imagens acima registram a interface anterior à inclusão de favoritos e ao ajuste de contraste dos cards.

## Funcionalidades

- Catálogo de populares com paginação e pull-to-refresh.
- Busca paginada a partir de três caracteres, com espera de 500 ms e cancelamento ao alterar a consulta.
- Consulta de detalhes com sinopse, avaliação, data e imagem.
- Adição e remoção de favoritos nos detalhes; lista acessível pelo coração na home.
- Favoritos persistidos localmente, disponíveis após fechar e reabrir o app.
- Estados de carregamento, erro, lista vazia e novas tentativas.
- Tema claro/escuro e grades adaptáveis à largura disponível.

## Dados e modo offline

A lista de populares é lida do Room e atualizada pelo `RemoteMediator`. Um refresh bem-sucedido substitui apenas o catálogo de populares e suas chaves de paginação. Se a atualização falhar, o conteúdo já armazenado continua visível.

Detalhes e favoritos ficam em tabelas independentes. Consultar um resultado da busca não adiciona esse filme à lista de populares. Para abrir detalhes sem conexão, o repositório tenta o cache de detalhes, os favoritos e, por último, o catálogo salvo. Novas buscas e páginas ainda não carregadas exigem internet. Imagens dependem do cache do Coil e podem não estar disponíveis offline.

O banco está na versão 3, com migração explícita da versão 2 preservando o catálogo existente. Não há expiração automática, criptografia do banco ou sincronização de favoritos entre dispositivos.

## Organização

Um módulo `app`, organizado em camadas:

```text
com.sbaldasso.tmdbapp/
├── data/          # Retrofit, Room, mappers, paginação e implementação do repositório
├── domain/        # Modelos, contrato do repositório e casos de uso
├── presentation/  # Telas Compose, ViewModels, estados, componentes e navegação
├── di/            # Módulos Hilt
└── ui/theme/      # Tema Material
```

A interface depende de ViewModels; os casos de uso acessam o contrato `MovieRepository`; a implementação coordena rede e persistência. O domínio usa `PagingData`, portanto não é completamente independente do Android Jetpack. A separação é feita por pacotes, sem multimódulos.

## Stack

Kotlin 1.9.22, Compose + Material 3, Navigation Compose, Hilt, Coroutines/Flow, Retrofit/OkHttp, Kotlinx Serialization, Room, Paging 3 e Coil. Os testes usam JUnit, MockK, Coroutines Test, Paging Testing e Robolectric.

As dependências efetivamente utilizadas estão em `app/build.gradle.kts`; o projeto não usa o catálogo de versões para resolvê-las.

## Executar

Requisitos: JDK 17, Android Studio compatível com AGP 8.3.0 e SDK Android 34. O aplicativo suporta Android 7.0 (API 24) ou superior. O Gradle Wrapper está incluído.

1. Clone o repositório e abra no Android Studio.
2. Configure o SDK pelo Android Studio ou pela variável `ANDROID_HOME`.
3. Crie `local.properties` na raiz, sem versionar:

```properties
sdk.dir=/caminho/para/Android/sdk
TMDB_API_KEY=sua_chave_v3
```

Também é possível fornecer `TMDB_API_KEY` como variável de ambiente; ela tem prioridade sobre o arquivo local. Obtenha uma chave v3 nas [configurações de API do TMDB](https://www.themoviedb.org/settings/api).

```bash
./gradlew assembleDebug
./gradlew installDebug
```

O build e os testes podem rodar sem chave. Nesse caso, chamadas ao catálogo online apresentam um erro de configuração. A chave não tem valor de fallback no código. Como é incorporada ao APK por `BuildConfig`, ela não deve ser tratada como um segredo protegido no dispositivo.

## Validação

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Os testes verificam:

- Conteúdo emitido pelo caso de uso de populares e pela HomeViewModel.
- Debounce, normalização, cancelamento e limpeza dos resultados da busca.
- Carregamento, erro, retry e falha ao salvar favoritos nos detalhes.
- Cache de detalhes independente, fallback offline e propagação de cancelamento.
- Fim da paginação e erros da busca paginada.
- Persistência e remoção de favoritos após reabrir o banco.
- Refresh preservando detalhes e favoritos, falha de rede preservando populares e migração 2 → 3 com validação do schema pelo Room.

Os testes de banco usam SQLite com Robolectric na JVM. O teste instrumentado de exemplo não representa cobertura dos fluxos de interface. Não há percentual de cobertura declarado nem benchmarks de tamanho, memória ou startup.

O workflow [Android checks](.github/workflows/android.yml) executa testes, lint e build em pushes e pull requests para `main`, sem credenciais do TMDB, e disponibiliza os relatórios como artefatos.

## Decisões e próximos passos

- Favoritos guardam uma cópia dos dados do filme, para sobreviver à renovação do catálogo.
- A busca cancela a espera e a coleta anterior ao mudar a consulta; consultas curtas limpam os resultados.
- A coleta de estados Compose acompanha o ciclo de vida.
- Cancelamento de coroutines é propagado, sem virar erro ou disparar fallback de cache.
- Cards usam gradiente escuro e título branco para melhorar a leitura sobre pôsteres.

Próximos passos: testes Compose dos fluxos completos, revisão de acessibilidade e fontes grandes, internacionalização dos textos, atualização planejada da toolchain e medição de performance. O target SDK atual é 34; requisitos de distribuição devem ser verificados antes de publicar.

## Autor e licença

[Samuel Baldasso](https://github.com/samuelbaldasso). Licença MIT, descrita em [LICENSE](LICENSE).

Este projeto utiliza a API do TMDB, mas não é endossado ou certificado pelo TMDB.
