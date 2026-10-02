import { Module } from '@nestjs/common';
import { CompServiciosService } from './comp-servicios.service.js';
import { ServiciosService } from './servicios.service.js';

@Module({
  providers: [CompServiciosService, ServiciosService],
  exports: [CompServiciosService],
})
export class CompServiciosModule {}
