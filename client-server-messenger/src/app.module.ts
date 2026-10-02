import { Module } from '@nestjs/common';
import { createObserveModule } from '@nestjs/observe';
import { AppController } from './app.controller.js';
import { AppService } from './app.service.js';
import { VistaConsolaModule } from './vista-consola/vista-consola.module.js';
import { VistaEscritorioModule } from './vista-escritorio/vista-escritorio.module.js';
import { FachadaServiciosModule } from './fachada-servicios/fachada-servicios.module.js';
import { MensajesModule } from './mensajes/mensajes.module.js';
import { LogEventosModule } from './log-eventos/log-eventos.module.js';
import { ConexionesClientesModule } from './conexiones-clientes/conexiones-clientes.module.js';
import { AlmacenarInformacionModule } from './almacenar-informacion/almacenar-informacion.module.js';
import { PersistenciaModule } from './persistencia/persistencia.module.js';

export const { ObserveModule, ObserveInstrument } = createObserveModule();

@Module({
  imports: [
    // Distributed tracing, auto-correlated logs, request/job metrics, error
    // telemetry, alarms, and more — out of the box. Sign up at https://observe.nestjs.com
    ObserveModule.forRoot({
      appKey: 'YOUR_APP_KEY',
      appSecret: 'YOUR_APP_SECRET',
      serviceId: 'client-server-messenger',
    }),
    VistaConsolaModule,
    VistaEscritorioModule,
    FachadaServiciosModule,
    MensajesModule,
    LogEventosModule,
    ConexionesClientesModule,
    AlmacenarInformacionModule,
    PersistenciaModule,
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
